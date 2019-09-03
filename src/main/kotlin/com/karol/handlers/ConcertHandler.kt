package com.karol.handlers

import com.karol.domain.Concert
import com.karol.domain.ConcertDto
import com.karol.domain.UserDetails
import com.karol.domain.Venue
import com.karol.services.ConcertService
import com.karol.services.VenueService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class ConcertHandler{

    @Autowired lateinit var venueService: VenueService
    @Autowired lateinit var concertService: ConcertService
    val DEFAULT_SIZE = 5L
    val DEFAULT_PAGE = 0L


    fun findConcertsByNameResponse(name: String, by: String?, direction: String?, size: Int?, page: Int?) =
        ServerResponse.ok().body(concertsByNameLikeFlux(name, by, direction, size, page))

    fun concertsByNameLikeFlux(name: String, by: String?, direction: String?, size: Int?, page: Int?) =
            concertService.findByNameLike(name, by, direction, size, page).switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))

    fun findConcertsBySearchParams(name: String?, by: String?, direction: String?, size: Long?, page: Long?, venues: List<String>?, before: LocalDateTime?, after: LocalDateTime?) =
            ServerResponse.ok().body(concertWithSearchParamsFlux(name, by, direction, size, page, venues, before, after))

    fun concertWithSearchParamsFlux(name: String?, by: String?, direction: String?, size: Long?, page: Long?, venues: List<String>?, before: LocalDateTime?, after: LocalDateTime?): Flux<Concert> =
            concertService.findAll(name = name, direction = direction, by = by)
                    .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .filter{ if(before != null) it.date.isBefore(before) else true}
                    .filter{ if(after != null) it.date.isAfter(after) else true}
                    .filter { if(venues != null) it.venue.name in venues else true }
                    .skip((size?:DEFAULT_SIZE)*(page?:DEFAULT_PAGE))
                    .take(size?:DEFAULT_SIZE)
    fun findByIdResponse(req: ServerRequest): Mono<ServerResponse> = ServerResponse.ok()
            .body(concertService.findById(req.pathVariable("concertId")).switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND))))
    fun patchConcertMono(concertId: String, concertDto: Mono<ConcertDto>, venueId: String?): Mono<Concert> = venueService.findById(venueId?:"")
            .zipWith(concertDto)
            .flatMap { concertService.patchById(concertId = concertId, venue = if(it.t1.id == null) null else it.t1, concertDto = it.t2) }
    fun patchByIdResponse(req: ServerRequest): Mono<ServerResponse> = ServerResponse.accepted()
            .body(patchConcertMono(concertId = req.pathVariable("concertId"), concertDto = req.bodyToMono(ConcertDto::class.java), venueId = req.queryParam("venueId").orElse(null)))
    fun deleteByIdResponse(req: ServerRequest): Mono<ServerResponse> = ServerResponse.accepted()
            .body(concertService.deleteById(req.pathVariable("concertId")))

}