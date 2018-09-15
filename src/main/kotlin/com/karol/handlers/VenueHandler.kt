package com.karol.handlers

import com.karol.services.ConcertService
import com.karol.services.VenueService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.toMono

@Component
class VenueHandler {
    @Autowired lateinit var venueService: VenueService
    @Autowired lateinit var concertService: ConcertService

    fun findConcertsByVenueName(venueName: String) = ServerResponse.ok().body(concertsByVenueNameFlux(venueName))

    fun concertsByVenueNameFlux(venueName: String) = venueService.findByName(venueName)
            .flatMapMany { concertService.findAllByVenueId(it?.id?: "")}


}