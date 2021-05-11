package org.fsalah.config

import org.fsalah.config.domain.DataSourceConfig
import org.fsalah.config.domain.Env
import org.fsalah.config.domain.KafkaConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class EnvConfig(
        @Value("\${application.env}") val appEnv: String,
        @Value("\${kafka.servers}") val servers: String,
        @Value("\${kafka.topic}") val topic: String,
        @Value("\${kafka.consumerId}") val consumerId: String,

        @Value("\${spring.datasource1.db}") val dataSource1Db: String,
        @Value("\${spring.datasource1.host}") val dataSource1Host: String,
        @Value("\${spring.datasource1.port}") val dataSource1Port: Int,
        @Value("\${spring.datasource1.username}") val dataSource1Username: String,
        @Value("\${spring.datasource1.password}") val dataSource1Password: String,
        @Value("\${spring.datasource1.maximum-pool-size}") val dataSource1PoolSize: Int,

        @Value("\${spring.datasource2.db}") val dataSource2Db: String,
        @Value("\${spring.datasource2.host}") val dataSource2Host: String,
        @Value("\${spring.datasource2.port}") val dataSource2Port: Int,
        @Value("\${spring.datasource2.username}") val dataSource2Username: String,
        @Value("\${spring.datasource2.password}") val dataSource2Password: String,
        @Value("\${spring.datasource2.maximum-pool-size}") val dataSource2PoolSize: Int
) {
    @Bean
    @Primary
    fun env(): Env {
        return Env(
                env = appEnv,
                kafkaConfig = KafkaConfig(
                        servers = servers,
                        topic = topic,
                        consumerId = consumerId
                ),
                dataSource1 = DataSourceConfig(
                        db = dataSource1Db,
                        host = dataSource1Host,
                        port = dataSource1Port,
                        username = dataSource1Username,
                        password = dataSource1Password,
                        maximumPoolSize = dataSource1PoolSize
                ),
                dataSource2 = DataSourceConfig(
                        db = dataSource2Db,
                        host = dataSource2Host,
                        port = dataSource2Port,
                        username = dataSource2Username,
                        password = dataSource2Password,
                        maximumPoolSize = dataSource2PoolSize
                )
        )
    }
}
