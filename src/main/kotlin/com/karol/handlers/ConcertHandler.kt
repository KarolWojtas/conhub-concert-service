package com.karol.handlers

import com.karol.domain.Concert
import com.karol.domain.UserDetails
import com.karol.services.ConcertService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class ConcertHandler{
    @Qualifier("loadBalancedWebClientBuilder")
    @Autowired lateinit var webClientBuilder: WebClient.Builder
    @Autowired lateinit var concertService: ConcertService
    val DEFAULT_SIZE = 5L
    val DEFAULT_PAGE = 0L

    fun findUserByUsername(username: String) = webClientBuilder.build()
            .get().uri("http://conhub-user-service/$username").retrieve()
            .onStatus(HttpStatus::is4xxClientError){ Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND,"User with username: $username was not found"))}
            .bodyToMono(UserDetails::class.java)
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

}