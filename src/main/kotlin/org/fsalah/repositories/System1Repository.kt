package org.fsalah.repositories

import io.vertx.core.json.JsonObject
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple
import org.fsalah.config.Fault
import org.fsalah.events.EventEnvelope
import org.fsalah.logger
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class System1Repository(private val dataSource1: PgPool) {

    fun save(consumer: String, partition: Int, offset: Int, eventEnvelope: EventEnvelope): Mono<Int> {
        logger.info("saving eventEnvelope into dataSource1 ")

        if (Fault.isFault.get()) {
            return Mono.error(IllegalStateException("fault"))
        }

        val query = """
                INSERT INTO events (operation, consumer, kafka_partition, kafka_offset, event_envelope) VALUES ($1, $2, $3, $4, $5) 
            """


        return Mono.create { sink ->
            dataSource1.preparedQuery(query)
                    .execute(
                            Tuple.of(
                                    "SAVE",
                                    consumer,
                                    partition,
                                    offset,
                                    JsonObject(eventEnvelope.toJson().stringify()),
                            )
                    ) { ar ->
                        if (!ar.succeeded()) {
                            sink.error(ar.cause())
                        } else {
                            val res = ar.result()
                            sink.success(res.rowCount())
                        }
                    }
        }

    }

    fun update(consumer: String, partition: Int, offset: Int, eventEnvelope: EventEnvelope): Mono<Int> {
        logger.info("saving eventEnvelope into dataSource1 ")

        if (Fault.isFault.get()) {
            return Mono.error(IllegalStateException("fault"))
        }
        val query = """
                INSERT INTO events (operation, consumer, kafka_partition, kafka_offset, event_envelope) VALUES ($1, $2, $3, $4, $5)
            """

        return Mono.create { sink ->
            dataSource1.preparedQuery(query)
                    .execute(
                            Tuple.of(
                                    "UPDATE",
                                    consumer,
                                    partition,
                                    offset,
                                    JsonObject(eventEnvelope.toJson().stringify()),
                            )
                    ) { ar ->
                        if (!ar.succeeded()) {
                            sink.error(ar.cause())
                        } else {
                            val res = ar.result()
                            sink.success(res.rowCount())
                        }
                    }
        }
    }

    fun delete(consumer: String, partition: Int, offset: Int, eventEnvelope: EventEnvelope): Mono<Int> {
        logger.info("saving eventEnvelope into dataSource1 ")

        if (Fault.isFault.get()) {
            return Mono.error(IllegalStateException("fault"))
        }
        val query = """
                INSERT INTO events (operation, consumer, kafka_partition, kafka_offset, event_envelope) VALUES ($1, $2, $3, $4, $5) 
            """

        return Mono.create { sink ->
            dataSource1.preparedQuery(query)
                    .execute(
                            Tuple.of(
                                    "DELETE",
                                    consumer,
                                    partition,
                                    offset,
                                    JsonObject(eventEnvelope.toJson().stringify()),
                            )
                    ) { ar ->
                        if (!ar.succeeded()) {
                            sink.error(ar.cause())
                        } else {
                            val res = ar.result()
                            sink.success(res.rowCount())
                        }
                    }
        }
    }
}
