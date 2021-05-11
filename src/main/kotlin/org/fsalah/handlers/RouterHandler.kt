package org.fsalah.handlers

import io.vertx.pgclient.pubsub.PgSubscriber
import org.fsalah.config.Fault
import org.fsalah.events.EventEnvelope
import org.fsalah.events.EventType
import org.fsalah.events.Payload
import org.fsalah.logger
import org.fsalah.producers.EventsProducer
import org.reactivecouchbase.json.Json
import org.reactivecouchbase.json.Syntax.`$`
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromPublisher
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@Component
class RouterHandler constructor(val producer: EventsProducer, private val subscriber: PgSubscriber) {

    lateinit var subscription: Disposable

    fun startEmitting(request: ServerRequest): Mono<ServerResponse> {
        startPublishing()
        return ServerResponse.ok().bodyValue("event published")
    }

    fun stopEmitting(request: ServerRequest): Mono<ServerResponse> {
        subscription.dispose()
        return ServerResponse.ok().bodyValue("event stopped")
    }

    private fun asyncEdition(): Flux<String> {
        return Flux.create { emitter ->
            val channel = "events_queue"
            subscriber.channel(channel).handler { evt: String ->
                logger.info(" --- received evt {} on {}", evt, channel)
                emitter.next(evt)
            }.exceptionHandler { error ->
                logger.error(" --- got error on {}", channel, error)
                emitter.error(error)
            }.endHandler {
                logger.info(" ---- subscriber has completed")
                emitter.complete()
            }
        }
    }

    fun injectFault(request: ServerRequest): Mono<ServerResponse> {
        Fault.isFault.set(true)
        return ServerResponse.ok().bodyValue("fault injected")
    }

    fun recover(request: ServerRequest): Mono<ServerResponse> {
        Fault.isFault.set(false)
        return ServerResponse.ok().bodyValue("consumer recovered")
    }

    fun records(request: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_STREAM_JSON)
                .body(fromPublisher(asyncEdition().map { record -> record + "\n\n" }, String::class.java))
    }

    private fun startPublishing() {
        subscription = producer.publish {
            Flux.interval(Duration.ofSeconds(3)).map {
                val entityId = UUID.randomUUID()
                EventEnvelope(
                        uuid = UUID.randomUUID(),
                        type = EventType.CREATE,
                        entityId = entityId,
                        payload = Payload(
                                id = entityId.toString(),
                                data = Json.obj(
                                        `$`("firstName", "Fedy"),
                                        `$`("lastName", "SALAH"),
                                        `$`("id", entityId.toString())
                                )
                        )
                )
            }
        }.subscribe()
    }
}