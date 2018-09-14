package com.karol.handlers

import com.karol.domain.Concert
import com.karol.services.ConcertService
import com.karol.services.VenueService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class VenueHandler {
    @Autowired lateinit var venueService: VenueService
    @Autowired lateinit var concertService: ConcertService

    fun findConcertsByVenueName(request: ServerRequest): Mono<ServerResponse> {
        val venueName = request.pathVariable("venueName")
        return ServerResponse.ok().body(venueService.findByName(venueName)
                .flatMapMany { concertService.findAllByVenueId(it?.id?: "")})
    }

}