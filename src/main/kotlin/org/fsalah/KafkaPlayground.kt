package org.fsalah

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaPlayground

fun main(args: Array<String>) {
    runApplication<KafkaPlayground>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}