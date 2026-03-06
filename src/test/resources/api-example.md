# Plantation API Examples

This document outlines the standard request and response JSON structures for the `PlantationController`.

## 1. Create Plantation (`POST /api/plantations`)

### Request
```json
{
  "name": "Sungei Bahar Estate",
  "location": "Jambi, Indonesia",
  "area": 1250.75,
  "description": "Main palm oil estate in Jambi region",
  "ownerId": "usr-8a7b6c5d",
  "plantDate": "2020-05-15T08:00:00"
}
```

### Response (201 Created)
```json
{
  "id": 1,
  "code": "PLT-A1B2C3D4",
  "name": "Sungei Bahar Estate",
  "location": "Jambi, Indonesia",
  "area": 1250.75,
  "description": "Main palm oil estate in Jambi region",
  "ownerId": "usr-8a7b6c5d",
  "plantDate": "2020-05-15T08:00:00",
  "createdAt": "2025-10-21T14:30:15.123456",
  "updatedAt": "2025-10-21T14:30:15.123456"
}
```

---

## 2. Get All Plantations (`GET /api/plantations`)

### Response (200 OK)
```json
[
  {
    "id": 1,
    "code": "PLT-A1B2C3D4",
    "name": "Sungei Bahar Estate",
    "location": "Jambi, Indonesia",
    "area": 1250.75,
    "description": "Main palm oil estate in Jambi region",
    "ownerId": "usr-8a7b6c5d",
    "plantDate": "2020-05-15T08:00:00",
    "createdAt": "2025-10-21T14:30:15.123",
    "updatedAt": "2025-10-21T14:30:15.123"
  },
  {
    "id": 2,
    "code": "PLT-E5F6G7H8",
    "name": "Rokan Hilir Block A",
    "location": "Riau, Indonesia",
    "area": 850.5,
    "description": "Secondary block",
    "ownerId": "usr-9z8y7x6w",
    "plantDate": "2018-11-20T09:30:00",
    "createdAt": "2025-10-22T08:15:22.456",
    "updatedAt": "2025-10-22T08:15:22.456"
  }
]
```

---

## 3. Get Plantation by ID (`GET /api/plantations/1`)

### Response (200 OK)
```json
{
  "id": 1,
  "code": "PLT-A1B2C3D4",
  "name": "Sungei Bahar Estate",
  "location": "Jambi, Indonesia",
  "area": 1250.75,
  "description": "Main palm oil estate in Jambi region",
  "ownerId": "usr-8a7b6c5d",
  "plantDate": "2020-05-15T08:00:00",
  "createdAt": "2025-10-21T14:30:15.123",
  "updatedAt": "2025-10-21T14:30:15.123"
}
```

---

## 4. Get Plantations by Owner (`GET /api/plantations/owner/usr-8a7b6c5d`)

### Response (200 OK)
```json
[
  {
    "id": 1,
    "code": "PLT-A1B2C3D4",
    "name": "Sungei Bahar Estate",
    "location": "Jambi, Indonesia",
    "area": 1250.75,
    "description": "Main palm oil estate in Jambi region",
    "ownerId": "usr-8a7b6c5d",
    "plantDate": "2020-05-15T08:00:00",
    "createdAt": "2025-10-21T14:30:15.123",
    "updatedAt": "2025-10-21T14:30:15.123"
  }
]
```

---

## 5. Update Plantation (`PUT /api/plantations/1`)

### Request
```json
{
  "name": "Sungei Bahar Estate (Updated)",
  "location": "Jambi, Indonesia",
  "area": 1300.0,
  "description": "Expanded palm oil estate in Jambi region",
  "ownerId": "usr-8a7b6c5d",
  "plantDate": "2020-05-15T08:00:00"
}
```

### Response (200 OK)
```json
{
  "id": 1,
  "code": "PLT-A1B2C3D4",
  "name": "Sungei Bahar Estate (Updated)",
  "location": "Jambi, Indonesia",
  "area": 1300.0,
  "description": "Expanded palm oil estate in Jambi region",
  "ownerId": "usr-8a7b6c5d",
  "plantDate": "2020-05-15T08:00:00",
  "createdAt": "2025-10-21T14:30:15.123",
  "updatedAt": "2025-10-25T09:45:10.987"
}
```

---

## 6. Delete Plantation (`DELETE /api/plantations/1`)

### Response (204 No Content)
*(Empty body)*
