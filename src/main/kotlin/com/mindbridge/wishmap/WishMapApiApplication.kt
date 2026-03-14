package com.mindbridge.wishmap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class WishMapApiApplication

fun main(args: Array<String>) {
    runApplication<WishMapApiApplication>(*args)
}
