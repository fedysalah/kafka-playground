package org.fsalah.config

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.pgclient.pubsub.PgSubscriber
import io.vertx.sqlclient.PoolOptions
import org.fsalah.config.domain.Env
import org.fsalah.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class AppConfig {

    @Bean
    fun verticle(): Vertx {
        return Vertx.vertx()
    }

    @Bean
    fun pubSubClient(env: Env, verticle: Vertx): PgSubscriber {
        val connectOptions = PgConnectOptions()
                .setPort(env.dataSource1.port)
                .setHost(env.dataSource1.host)
                .setDatabase(env.dataSource1.db)
                .setUser(env.dataSource1.username)
                .setPassword(env.dataSource1.password)
                .setConnectTimeout(20 * 1000) // 20s
                .setTcpKeepAlive(true)

        val subscriber = PgSubscriber.subscriber(verticle, connectOptions)

        return subscriber.connect { ar: AsyncResult<Void> ->
            if (ar.succeeded()) {
                logger.info("Postgres subscriber connected")
            }
        }
    }

    @Bean("dataSource1")
    fun asyncClient1(env: Env, verticle: Vertx): PgPool {
        return PgPool.pool(
                verticle,
                PgConnectOptions()
                        .setPort(env.dataSource1.port)
                        .setHost(env.dataSource1.host)
                        .setDatabase(env.dataSource1.db)
                        .setUser(env.dataSource1.username)
                        .setPassword(env.dataSource1.password)
                        .setIdleTimeout(30),
                PoolOptions().setMaxSize(env.dataSource1.maximumPoolSize)
        )
    }

    @Bean("dataSource2")
    fun asyncClient2(env: Env, verticle: Vertx): PgPool {
        return PgPool.pool(
                verticle,
                PgConnectOptions()
                        .setPort(env.dataSource2.port)
                        .setHost(env.dataSource2.host)
                        .setDatabase(env.dataSource2.db)
                        .setUser(env.dataSource2.username)
                        .setPassword(env.dataSource2.password)
                        .setIdleTimeout(30),
                PoolOptions().setMaxSize(env.dataSource1.maximumPoolSize)
        )
    }
}