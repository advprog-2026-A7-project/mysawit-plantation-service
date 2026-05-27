# Canary Deployment + Load Balancing — plantation-service

## Strategi yang dipilih

**Canary** dengan **Load Balancing** sebagai prosedur lanjutan.

- **Stable pool** = 2 replica plantation-service (`stable-1` di port 8182, `stable-2` di port 8192). Traffic dibagi antar keduanya oleh nginx (`least_conn`).
- **Canary** = 1 replica versi baru (port 8282). Nginx mengarahkan persentase kecil traffic (default 10%) ke canary, sisanya ke stable pool.
- **Promote** jika canary aman: rolling-replace setiap stable replica dengan image canary, lalu hapus container canary.
- **Rollback** jika canary error: nginx instan kembali 100% ke stable pool, container canary dihapus.

Production CD container (port 8082) **tetap berjalan** terpisah — demo ini di port 8089 (nginx) + 8182/8192/8282.

## Alasan pemilihan

plantation-service punya banyak business logic non-trivial:
- Validasi overlap koordinat kebun (GeometryValidator)
- Constraint delete kebun (FK ke harvest, assignment)
- Assignment Mandor/Supir + event publishing
- Konsumsi `user.deleted` event dari identity

Canary cocok karena:
- **Blast radius kecil** — bug di logic baru hanya menyentuh 10% user, bukan semua
- **Real traffic, real DB** — caught issues yang tidak muncul di staging
- **Progressive ramp-up** — naikkan 10% → 30% → 50% → promote

Load Balancing cocok karena plantation read-heavy (list kebun, list assignment) — multi-replica meratakan request dan jadi *building block* yang dibutuhkan canary (canary pool ada di samping stable pool).

Trade-off: butuh ~3× RAM saat canary aktif (stable-1 + stable-2 + canary). Set `-Xmx384m` per replica di `.env.deploy` untuk EC2 kecil.

## Layout port

| Komponen | Host port | Akses |
|---|---|---|
| nginx (front door demo) | 8089 | Buka di Security Group |
| stable-1 | 8182 | Internal (via nginx) |
| stable-2 | 8192 | Internal |
| canary | 8282 | Internal — tapi bisa diakses langsung untuk testing |
| Production CD container | 8082 | Independen, tetap berjalan |
| Prometheus | 9090 | Scrape semua slot |
| Grafana | 3000 | Filter `slot` label |

## Pre-flight (sekali)

```bash
ssh -i ~/.ssh/mysawit-plantation.pem ec2-user@<PLANTATION_EC2_IP>
chmod +x ~/mysawit-plantation-service/scripts/*.sh
docker image ls mysawit-plantation
```

Tambah inbound rule di Security Group plantation: port `8089` TCP, source `0.0.0.0/0`.

## Command demo

Semua dijalankan dari **EC2**.

### 1. Spin up load balancer + 2 stable replicas

```bash
~/mysawit-plantation-service/scripts/lb-up.sh
# atau: lb-up.sh <image-tag>
```

Verify LB membagi request:
```bash
for i in {1..20}; do
  curl -s http://localhost:8089/actuator/health -o /dev/null \
       -w "%{http_code} via %{remote_ip}:%{remote_port}\n"
done
```

Cek distribution kasar:
```bash
~/mysawit-plantation-service/scripts/status.sh
```

### 2. Deploy canary dengan initial weight 10%

Pastikan ada image canary. Cara termudah — tag latest sebagai canary-test:
```bash
docker tag mysawit-plantation:latest mysawit-plantation:canary-test
```

Lalu deploy canary:
```bash
~/mysawit-plantation-service/scripts/canary-deploy.sh canary-test 10
```

Verify split:
```bash
for i in {1..20}; do curl -s http://localhost:8089/__backend; done | sort | uniq -c
# Output kira-kira:
#   18 stable_pool
#    2 canary_pool
```

### 3. Naikkan traffic canary bertahap

```bash
~/mysawit-plantation-service/scripts/canary-weight.sh 30   # 30%
~/mysawit-plantation-service/scripts/canary-weight.sh 50   # 50%
~/mysawit-plantation-service/scripts/canary-weight.sh 80   # 80%
```

Setelah tiap step, observe Grafana selama beberapa menit. Bandingkan error rate `slot=canary` vs `slot=stable`. Kalau tidak ada degradasi → naikkan lagi.

### 4. Rollback canary (jika error)

```bash
~/mysawit-plantation-service/scripts/canary-rollback.sh
```

Sub-detik switch — nginx reload bersifat graceful, koneksi existing tidak putus. Canary container dihapus.

### 5. Promote canary (jika lulus)

```bash
~/mysawit-plantation-service/scripts/canary-promote.sh
```

Replace stable-1 → tunggu readiness → replace stable-2 → hapus canary. Image canary sekarang menjadi versi stable.

### 6. Status

```bash
~/mysawit-plantation-service/scripts/status.sh
```

### 7. Cleanup setelah demo

```bash
~/mysawit-plantation-service/scripts/lb-down.sh
```

## Verifikasi metrics

### Prometheus targets (http://EC2:9090/targets)

Saat LB jalan + canary deployed, expect 4 target plantation `UP`:
- `mysawit-plantation-service` slot=prod  (port 8082)
- `mysawit-plantation-stable`  slot=stable (port 8182 & 8192, dua entri)
- `mysawit-plantation-canary`  slot=canary (port 8282)

PromQL untuk demo:
```promql
# Request rate per slot — terlihat stable_pool ~90% dan canary ~10%
sum by (slot) (rate(http_server_requests_seconds_count{service="mysawit-plantation-service"}[1m]))

# Error rate per slot — kalau canary >> stable, INDIKATOR ROLLBACK
sum by (slot) (rate(http_server_requests_seconds_count{service="mysawit-plantation-service",status=~"5.."}[1m]))

# P95 latency per slot + endpoint
histogram_quantile(0.95,
  sum by (le, uri, slot) (rate(http_server_requests_seconds_bucket{service="mysawit-plantation-service"}[5m])))

# DB query latency proxy (HikariCP)
rate(hikaricp_connections_usage_seconds_sum{service="mysawit-plantation-service"}[1m])
/ rate(hikaricp_connections_usage_seconds_count{service="mysawit-plantation-service"}[1m])

# Custom counters (perlu wire ke kode dulu)
rate(plantation_overlap_validation_failure_total[1m])
rate(plantation_delete_failure_total[1m])
rate(plantation_assignment_failure_total[1m])

# CPU per instance
process_cpu_usage{service="mysawit-plantation-service"}

# Memory per instance
sum by (slot, instance, area) (jvm_memory_used_bytes{service="mysawit-plantation-service"})
```

### Grafana (http://EC2:3000)

Import dashboard ID **`4701`** (JVM) and **`12900`** (Spring Boot HTTP) untuk panel siap pakai. Filter dengan variable `slot` untuk membandingkan stable vs canary.

## Custom counters — cara wire ke kode

```java
// PlantationService.java
@Service
public class PlantationService {
    private final Counter overlapValidationFailureCounter;
    private final Counter deletePlantationFailureCounter;
    private final Counter assignmentFailureCounter;
    private final Counter assignmentSuccessCounter;
    private final Timer assignmentLatencyTimer;
    // ...

    public PlantationService(Counter overlapValidationFailureCounter,
                             Counter deletePlantationFailureCounter,
                             Counter assignmentFailureCounter,
                             Counter assignmentSuccessCounter,
                             Timer assignmentLatencyTimer,
                             /* existing deps */) {
        this.overlapValidationFailureCounter = overlapValidationFailureCounter;
        // ... assign others
    }

    public Plantation create(CreatePlantationRequest req) {
        if (overlaps(req.getCoordinates())) {
            overlapValidationFailureCounter.increment();
            throw new OverlapException(...);
        }
        // ... save
    }

    public void delete(Long id) {
        try {
            // ... delete logic
        } catch (DataIntegrityViolationException e) {
            deletePlantationFailureCounter.increment();
            throw e;
        }
    }

    public void assignMandor(Long plantationId, String mandorId) {
        assignmentLatencyTimer.record(() -> {
            try {
                // ... existing logic
                assignmentSuccessCounter.increment();
            } catch (Exception e) {
                assignmentFailureCounter.increment();
                throw e;
            }
        });
    }
}
```

## Catatan resource

EC2 `t3.small` (2 GB): set `JAVA_OPTS=-Xms128m -Xmx384m` di `.env.deploy` sehingga 3 replica + nginx muat.

EC2 `t3.micro` (1 GB): hentikan production CD container saat demo:
```bash
docker stop mysawit-plantation; docker start mysawit-plantation
```

## Bukti prosedur berjalan

Untuk laporan kuliah, capture:

1. **`status.sh`** menunjukkan stable-1 + stable-2 + canary semua "Up"
2. **`for ... __backend`** output — ratio stable/canary sesuai weight
3. **Prometheus targets** ketiga slot UP
4. **Grafana panel "Request rate by slot"** — visualisasi 90/10, lalu 50/50 setelah `canary-weight.sh 50`
5. **Grafana panel "Error rate by slot"** — bila demo simulasi error di canary, terlihat spike di slot=canary
6. **Rollback demo** — `canary-rollback.sh` → traffic instant 100% kembali stable, error rate canary turun ke 0

## Risiko & batasan

- **Session affinity tidak ada** — `split_clients` hash by `$remote_addr$remote_port`. Satu user bisa di-route ke pool berbeda di request berurutan. Untuk stateful flow (form wizard), pertimbangkan `ip_hash` atau cookie-based sticky.
- **Database shared** — canary dan stable bicara ke DB Supabase yang sama. Bila canary mengandung migrasi yang merusak schema, stable juga ikut down. Jadi: hindari deploy canary dengan DDL breaking.
- **CloudAMQP shared** — canary mempublikasikan event ke broker yang sama. Consumer (shipment) akan menerima event dari kedua versi. Test backward-compat event schema sebelum canary.
- **Stale IP issue saat lab restart** — IP plantation EC2 sudah Elastic IP. Tapi bila lab di-restart, container `mysawit-plantation-stable-*` punya `--restart unless-stopped` jadi auto-up. Nginx juga begitu. Canary container TIDAK auto-restart pasca lab restart by design — biar tidak kembali "in canary mode" setelah lab berhenti.
