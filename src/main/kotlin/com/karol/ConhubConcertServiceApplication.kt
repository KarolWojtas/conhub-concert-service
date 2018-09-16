package com.karol

import com.karol.domain.Concert
import com.karol.domain.ConcertComment
import com.karol.domain.Venue
import com.karol.handlers.VenueHandler
import com.karol.repositories.ConcertCommentRepository
import com.karol.repositories.ConcertRepository
import com.karol.repositories.VenueRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

@SpringBootApplication
class ConhubConcertServiceApplication

fun main(args: Array<String>) {
    runApplication<ConhubConcertServiceApplication>(*args)
}
@Component
class Bootstrap : CommandLineRunner{
    @Autowired lateinit var concertRepository: ConcertRepository
    @Autowired lateinit var venueRepository: VenueRepository
    @Autowired lateinit var commentRepository: ConcertCommentRepository
    override fun run(vararg args: String?) {

        venueRepository.deleteAll().block()
        concertRepository.deleteAll().block()

        val ucho = Venue(id = null, name = "Ucho")
        val bc = Venue(id = null, name = "Blues Club")
        val concert1 = Concert(id = null, venue = ucho, name = "Disco w remizie", date = LocalDateTime.now())
        val concert2 = Concert(id = null, venue = ucho, name = "1 Rap impra stulecia", date = LocalDateTime.now())
        val concert3 = Concert(id = null, venue = ucho, name = "2 Rap impra stulecia", date = LocalDateTime.now())
        val concert4 = Concert(id = null, venue = ucho, name = "3 Rap impra stulecia", date = LocalDateTime.now())
        val concert5 = Concert(id = null, venue = bc, name = "4 Rap impra stulecia", date = LocalDateTime.now())

        venueRepository.saveAll(Flux.just(ucho,bc)).blockLast()
        concertRepository.saveAll(Flux.just(concert1, concert2, concert3, concert4, concert5)).blockLast()

        val comment = ConcertComment(id = null, text = "Ten koncert jest zajebisty!", concert = concertRepository.findByName(concert1.name).block()?:concert1, timestamp = LocalDateTime.now(), username = "username")
        commentRepository.save(comment).block()


        }
    }


