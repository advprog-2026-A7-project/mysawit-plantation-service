package com.mysawit.plantation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MysawitPlantationServiceApplication

fun main(args: Array<String>) {
    val context = runApplication<MysawitPlantationServiceApplication>(*args)
    if (context.environment.getProperty("app.test.close-context", Boolean::class.java, false)) {
        context.close()
    }
}
