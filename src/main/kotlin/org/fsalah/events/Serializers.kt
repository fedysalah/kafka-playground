package org.fsalah.events

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.reactivecouchbase.json.Json

class EventEnvelopeDeserializer : Deserializer<EventEnvelope> {
    private val stringDeserializer: StringDeserializer = StringDeserializer()

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        stringDeserializer.configure(configs, isKey)
    }

    override fun close() {
        stringDeserializer.close()
    }

    override fun deserialize(topic: String, data: ByteArray): EventEnvelope {
        val event = stringDeserializer.deserialize(topic, data)
        return EventEnvelope.from(Json.parse(event))
    }
}

class EventEnvelopeSerializer : Serializer<EventEnvelope> {

    private val stringSerializer: StringSerializer = StringSerializer()

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        stringSerializer.configure(configs, isKey)
    }

    override fun serialize(topic: String, data: EventEnvelope): ByteArray {
        val envelopeString: String = data.toJson().stringify()
        return stringSerializer.serialize(topic, envelopeString)
    }

    override fun close() {
        stringSerializer.close()
    }
}