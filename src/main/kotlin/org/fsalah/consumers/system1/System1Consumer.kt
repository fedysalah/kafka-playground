package org.fsalah.consumers.system1

import io.vavr.control.Option
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.fsalah.config.domain.Env
import org.fsalah.events.EventEnvelope
import org.fsalah.events.EventEnvelopeDeserializer
import org.fsalah.events.EventType
import org.fsalah.logger
import org.fsalah.repositories.System1Repository
import org.springframework.stereotype.Service
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kotlin.extra.retry.retryExponentialBackoff
import reactor.util.retry.Retry
import java.lang.IllegalStateException
import java.time.Duration
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@Service
class System1Consumer constructor(env: Env, private val system1Repository: System1Repository) {

    private var subscriptionRef: Option<Disposable> = Option.none()
    private val consumer: KafkaReceiver<String, EventEnvelope>
    private val consumerId = "system1-consumers-group-${env.kafkaConfig.consumerId}"

    init {
        val props = HashMap<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = env.kafkaConfig.servers
        props[ConsumerConfig.GROUP_ID_CONFIG] = "system1-consumers-group"
        props[ConsumerConfig.CLIENT_ID_CONFIG] = consumerId
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        val receiverOptions: ReceiverOptions<String, EventEnvelope> = ReceiverOptions.create<String, EventEnvelope>(props)
                .withKeyDeserializer(StringDeserializer())
                .withValueDeserializer(EventEnvelopeDeserializer())
                .subscription(setOf(env.kafkaConfig.topic))
                .addAssignListener { partitions -> logger.debug("onPartitionsAssigned {}", partitions) }
                .addRevokeListener { partitions -> logger.debug("onPartitionsRevoked {}", partitions) }
                .commitInterval(Duration.ZERO)
                .commitBatchSize(0)

        consumer = KafkaReceiver.create(receiverOptions)
    }

    @PostConstruct
    private fun startUp() {
        logger.info("system1-consumers-group-consumer1")
        val scheduler = Schedulers.newBoundedElastic(10, 20, "FLUX_DEFER", 10, true)
        // defer is needed to avoid Scheduler unavailable
        val subscription = Flux.defer { consumer.receive() }
                .retryWhen(Retry.fixedDelay(10, Duration.ofSeconds(2)))
                .groupBy { m -> m.receiverOffset().topicPartition() }
                .flatMap { partitionFlux ->
                    partitionFlux.publishOn(scheduler)
                            // use concat map for single thread
                            .concatMap { record ->
                                val value = record.value()
                                val receiverOffset = record.receiverOffset()
                                when (value.type) {
                                    EventType.CREATE -> {
                                        system1Repository.save(consumerId, partitionFlux.key().partition(), receiverOffset.offset().toInt(), value)
                                                .flatMap {
                                                    logger.info("got response while processing create event on {}, {}", partitionFlux.key(), it)
                                                    receiverOffset.commit()
                                                }
                                                .retryWhen(Retry.backoff(20, Duration.ofSeconds(5)).maxBackoff(Duration.ofSeconds(10)))
                                                .doOnError { err -> logger.error("got error while processing create event on ${partitionFlux.key()}", err) }

                                    }
                                    EventType.UPDATE -> {
                                        system1Repository.update(consumerId, partitionFlux.key().partition(), receiverOffset.offset().toInt(), value)
                                                .flatMap {
                                                    logger.info("got response while processing update event on {}, {}", partitionFlux.key(), it)
                                                    receiverOffset.commit()
                                                }
                                                .doOnError { err -> logger.error("got error while processing update event on ${partitionFlux.key()}", err) }
                                                .retryWhen(Retry.fixedDelay(60, Duration.ofSeconds(2))).retry()
                                    }
                                    EventType.DELETE -> {
                                        system1Repository.delete(consumerId, partitionFlux.key().partition(), receiverOffset.offset().toInt(), value)
                                                .flatMap {
                                                    logger.info("got response while processing delete event on {}, {}", partitionFlux.key(), it)
                                                    receiverOffset.commit()
                                                }
                                                .doOnError { err -> logger.error("got error while processing delete event on ${partitionFlux.key()}", err) }
                                                .retryWhen(Retry.fixedDelay(60, Duration.ofSeconds(2))).retry()
                                    }
                                }
                            }
                }
                .doOnError { err -> logger.error("an error occurred", err) }
                .subscribe()

        subscriptionRef = Option.some(subscription)
    }

    @PreDestroy
    fun destroy() {
        this.subscriptionRef.forEach { it.dispose() }
    }
}
