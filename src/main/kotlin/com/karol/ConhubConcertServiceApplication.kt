package com.karol

import com.karol.domain.Concert
import com.karol.domain.ConcertComment
import com.karol.domain.Venue
import com.karol.handlers.VenueHandler
import com.karol.repositories.ConcertCommentRepository
import com.karol.repositories.ConcertRepository
import com.karol.repositories.VenueRepository
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import org.springframework.util.ResourceUtils
import reactor.core.publisher.Flux
import java.io.File
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

        
        val ucho = Venue(id = null, name = "Ucho", avatar = null)
        val bc = Venue(id = null, name = "Blues Club", avatar = null)
        val uboga = Venue(id = null, name = "Uboga Krewna", avatar = null)
        val concert1 = Concert(id = null, venue = ucho, name = "Disco w remizie", date = LocalDateTime.now())
        val concert2 = Concert(id = null, venue = ucho, name = "1 Rap impra stulecia", date = LocalDateTime.now().plusDays(2))
        val concert3 = Concert(id = null, venue = ucho, name = "2 Rap impra stulecia", date = LocalDateTime.now().plusDays(12))
        val concert4 = Concert(id = null, venue = ucho, name = "3 Rap impra stulecia", date = LocalDateTime.now().plusMonths(1))
        val concert5 = Concert(id = null, venue = bc, name = "4 Rap impra stulecia", date = LocalDateTime.now().plusDays(5))
        val concert6 = Concert(id = null, name = "Krzysztof Krawczyk", date = LocalDateTime.now().plusDays(7).plusHours(2), venue = uboga)
        val concert7 = Concert(id = null, name = "Majka Jeżowska", date = LocalDateTime.now().minusHours(10), venue = bc)
        val concert8 = Concert(id = null, name = "Zdzisława Sośnicka", date = LocalDateTime.now().plusDays(6).minusHours(2), venue = uboga)
        val concert9 = Concert(id = null, name = "Deadmau5", date = LocalDateTime.now().plusMonths(3).plusDays(8), venue = uboga)

        venueRepository.saveAll(Flux.just(ucho,bc, uboga)).blockLast()
        concertRepository.saveAll(Flux.just(concert1, concert2, concert3, concert4, concert5, concert6, concert7, concert8, concert9)).blockLast()

        val comment = ConcertComment(id = null, text = "Ten koncert jest zajebisty!", concert = concertRepository.findByName(concert1.name).block()?:concert1, timestamp = LocalDateTime.now(), username = "username")
        commentRepository.save(comment).block()


        }

    }


