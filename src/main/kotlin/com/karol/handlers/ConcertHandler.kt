package com.karol.handlers

import com.karol.domain.UserDetails
import com.karol.services.ConcertService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Component
class ConcertHandler{
    @Qualifier("loadBalancedWebClientBuilder")
    @Autowired lateinit var webClientBuilder: WebClient.Builder
    @Autowired lateinit var concertService: ConcertService
    val DEFAULT_SIZE = 5
    val DEFAULT_PAGE = 0
    val DEFAULT_SORT = Sort.by("name").ascending()

    fun findUserByUsername(username: String) = webClientBuilder.build()
            .get().uri("http://conhub-user-service/$username").retrieve()
            .onStatus(HttpStatus::is4xxClientError){ Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND,"User with username: $username was not found"))}
            .bodyToMono(UserDetails::class.java)
    fun findConcertsByNameResponse(name: String, by: String?, direction: String?, size: Int?, page: Int?) =
        ServerResponse.ok().body(concertsByNameLikeFlux(name, by, direction, size, page))

    fun concertsByNameLikeFlux(name: String, by: String?, direction: String?, size: Int?, page: Int?) =
            concertService.findByNameLike(name, by, direction, size, page)

}