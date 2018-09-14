package com.karol

import com.karol.domain.Concert
import com.karol.domain.Venue
import com.karol.handlers.VenueHandler
import com.karol.repositories.ConcertRepository
import com.karol.repositories.VenueRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@SpringBootApplication
class ConhubConcertServiceApplication

fun main(args: Array<String>) {
    runApplication<ConhubConcertServiceApplication>(*args)
}
@Component
class Bootstrap : CommandLineRunner{
    @Autowired lateinit var concertRepository: ConcertRepository
    @Autowired lateinit var venueRepository: VenueRepository
    @Autowired lateinit var handler: VenueHandler
    override fun run(vararg args: String?) {

            venueRepository.deleteAll().block()
            concertRepository.deleteAll().block()

            val ucho = Venue(id = null, name = "Ucho")
            val concert1 = Concert(id = null, venue = ucho, name = "Disco w remizie")
            val concert2 = Concert(id = null, venue = ucho, name = "Rap impra stulecia")

            venueRepository.save(ucho).block()
            concertRepository.saveAll(Flux.just(concert1, concert2)).blockLast()


        }
    }


