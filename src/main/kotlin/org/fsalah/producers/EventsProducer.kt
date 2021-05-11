package org.fsalah.producers

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.UUIDSerializer
import org.fsalah.config.domain.Env
import org.fsalah.events.EventEnvelope
import org.fsalah.events.EventEnvelopeSerializer
import org.fsalah.logger
import org.springframework.stereotype.Service
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import java.util.*
import javax.annotation.PreDestroy

@Service
class EventsProducer(private val env: Env) {

    private val producer: KafkaSender<UUID, EventEnvelope>

    init {
        val props = HashMap<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = env.kafkaConfig.servers
        props[ProducerConfig.CLIENT_ID_CONFIG] = "events-producer"
        props[ProducerConfig.ACKS_CONFIG] = "all"
        props[ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION] = 1
        props[ProducerConfig.RETRIES_CONFIG] = 10
        producer = KafkaSender.create(
                SenderOptions.create<UUID, EventEnvelope>(props)
                        .withKeySerializer(UUIDSerializer())
                        .withValueSerializer(EventEnvelopeSerializer())
        )
    }

    fun publish(events: () -> Flux<EventEnvelope>): Flux<Unit> {
        return producer.send(
                events().map { evtEnvelope ->
                    SenderRecord.create(
                            ProducerRecord(env.kafkaConfig.topic, evtEnvelope.entityId, evtEnvelope), evtEnvelope
                    )
                })
                .doOnError { e -> logger.error("Send failed", e) }
                .map { r ->
                    val metadata: RecordMetadata = r.recordMetadata()
                    logger.debug(
                            """
                                Message ${r.correlationMetadata()} sent successfully,
                                topic-partition=${metadata.topic()}-${metadata.partition()}
                                offset=${metadata.offset()}
                                timestamp=${Date(metadata.timestamp())}
                            """
                    )
                }
    }

    @PreDestroy
    fun destroy() {
        this.producer.close()
    }
}


