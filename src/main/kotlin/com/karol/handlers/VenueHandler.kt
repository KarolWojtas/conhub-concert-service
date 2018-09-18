package com.karol.handlers

import com.karol.domain.VenueDto
import com.karol.services.ConcertService
import com.karol.services.VenueService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

@Component
class VenueHandler {
    @Autowired lateinit var venueService: VenueService
    @Autowired lateinit var concertService: ConcertService

    fun findConcertsByVenueName(venueName: String) = ServerResponse.ok().body(concertsByVenueNameFlux(venueName))

    fun concertsByVenueNameFlux(venueName: String) = venueService.findByName(venueName)
            .flatMapMany { concertService.findAllByVenueId(it?.id?: "")}

    fun findAllResponse() = ServerResponse.ok().body(venueService.findAll())

    fun findById(request: ServerRequest) = ServerResponse.ok()
            .body(venueService.findById(request.pathVariable("venueId"))
                    .filter { it.id != null }
                    .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND))))

    fun patchVenueMono(venueId: String, venueDto: Mono<VenueDto> ) =
            venueDto.flatMap { venueService.patchVenue(venueId = venueId, venueDto = it) }
    fun patchById(req: ServerRequest) = ServerResponse.accepted()
            .body(patchVenueMono(venueId = req.pathVariable("venueId"), venueDto = req.bodyToMono(VenueDto::class.java)))
    fun deleteById(req: ServerRequest) = ServerResponse.accepted().body(venueService.deleteById(req.pathVariable("venueId")))
}