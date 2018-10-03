package com.karol

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class ConhubConcertServiceApplication

fun main(args: Array<String>) {
    SpringApplication(ConhubConcertServiceApplication::class.java).apply {
        run(*args)
    }
}




