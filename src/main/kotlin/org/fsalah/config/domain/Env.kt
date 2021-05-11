package org.fsalah.config.domain

data class Env(
        val env: String,
        val kafkaConfig: KafkaConfig,
        val dataSource1: DataSourceConfig,
        val dataSource2: DataSourceConfig
)

data class KafkaConfig(
        val servers: String,
        val topic: String,
        val consumerId: String
)

data class DataSourceConfig(
        val db: String,
        val host: String,
        val port: Int,
        val username: String,
        val password: String,
        val maximumPoolSize: Int
)