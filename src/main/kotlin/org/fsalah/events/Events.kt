package org.fsalah.events

import org.reactivecouchbase.json.JsValue
import org.reactivecouchbase.json.Json
import org.reactivecouchbase.json.Syntax.`$`
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

enum class EventType {
    CREATE, UPDATE, DELETE
}

data class EventEnvelope(
        val uuid: UUID,
        val type: EventType,
        val version: Int = 1,
        val at: LocalDateTime = LocalDateTime.now(),
        val entityId: UUID,
        val payload: Payload
) {

    fun toJson(): JsValue {
        return Json.obj(
                `$`("uuid", uuid.toString()),
                `$`("type", type.name),
                `$`("version", version),
                `$`("at", at.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
                `$`("entityId", entityId.toString()),
                `$`("payload", payload.toJson())
        )
    }

    companion object {
        fun from(json: JsValue): EventEnvelope {
            return EventEnvelope(
                    entityId = UUID.fromString(json.string("entityId")),
                    uuid = UUID.fromString(json.string("uuid")),
                    type = EventType.valueOf(json.string("type")),
                    version = json.integer("version"),
                    at = LocalDateTime.parse(json.string("at"), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    payload = Payload.from(json.field("payload"))
            )
        }
    }
}

data class Payload(
        val id: String,
        val data: JsValue

) {
    fun toJson(): JsValue {
        return Json.obj(
                `$`("id", id),
                `$`("data", data)
        )
    }

    companion object {
        fun from(json: JsValue): Payload {
            return Payload(
                    id = json.string("id"),
                    data = json.field("data")
            )
        }
    }
}

data class Record(
        val partition: Int,
        val offset: Int,
        val operation: String,
        val consumer: String,
        val envelope: EventEnvelope
) {
    fun toJson(): JsValue {
        return Json.obj(
                `$`("consumer", consumer),
                `$`("partition", partition),
                `$`("offset", offset),
                `$`("evt", Json.obj(
                        `$`("operation", operation),
                        `$`("envelope", envelope.toJson())
                ))

        )
    }
}

