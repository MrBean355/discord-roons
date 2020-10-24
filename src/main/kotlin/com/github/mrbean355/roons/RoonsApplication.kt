package com.github.mrbean355.roons

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling
import org.telegram.telegrambots.ApiContextInitializer

@SpringBootApplication
@EnableScheduling
@EnableCaching
class RoonsApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            ApiContextInitializer.init()
            SpringApplication.run(RoonsApplication::class.java, *args)
        }
    }
}
