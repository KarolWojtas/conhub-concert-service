package com.karol.domain

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

@Document data class Concert(
        @Id val id: String? = null,
        @TextIndexed val name: String,
        @JsonSerialize(using = VenueNameSerializer::class) @DBRef val venue: Venue,
        @DBRef val comments: List<ConcertComment>?,
        val date: LocalDateTime
)

@Document data class Venue(@Id val id: String? = null,
                           val name: String)
data class UserDetails(val id: Long, val username: String)

@Document data class ConcertComment(@Id val id: String? = null, val text: String, val userId: Long, @DBRef val concert: Concert, val timestamp: LocalDateTime)

class VenueNameSerializer : StdSerializer<Venue>(Venue::class.java){
    override fun serialize(venue: Venue?, jgen: JsonGenerator?, p2: SerializerProvider?) {
        jgen?.apply {
            writeStartObject()
            writeStringField("name", venue?.name)
            writeEndObject()
        }

    }

}