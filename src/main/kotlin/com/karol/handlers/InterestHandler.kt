package com.karol.handlers

import com.karol.domain.Concert
import com.karol.domain.Interest
import com.karol.domain.InterestDto
import com.karol.services.ConcertService
import com.karol.services.InterestService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class InterestHandler{
    @Autowired
    lateinit var interestService: InterestService
    @Autowired
    lateinit var concertService: ConcertService
    @Autowired
    lateinit var webClientService: WebClientService

    fun concertIndicesByUsernameFlux(username: String): Flux<String> = concertsByUsernameFlux(username)
            .map { it.id }

    fun concertsByUsernameFlux(username: String): Flux<Concert> = interestService.findAllByUsername(username)
            .filterWhen{webClientService.checkUsernameAsync(username)}
            .flatMap{ concertService.findById(it.concertId)}


    fun findConcertsByUsernameIntrestsResponse(req: ServerRequest): Mono<ServerResponse> {
        val indices = req.queryParam("indices").orElse("false").toBoolean()
        val username = req.pathVariable("username")
        return when(indices){
            true -> ServerResponse.ok().body(concertIndicesByUsernameFlux(username))
            else -> ServerResponse.ok().body(concertsByUsernameFlux(username))
        }
    }

    fun postInterestMono(interestDto: Mono<InterestDto>, username: String): Mono<Interest> = interestDto.flatMap { concertService.findById(it.concertId) }
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND,"Concert not found")))
            .filterWhen { webClientService.checkUsernameAsync(username) }

            .map { Interest(id = null, username = username, concertId = it.id?:"null") }
            .filter { it.concertId != "null" }
            .flatMap { interestService.postInterest(it) }
    fun postInterestResponse(req: ServerRequest): Mono<ServerResponse> = with(req){
        ServerResponse.accepted().body(postInterestMono(interestDto = this.bodyToMono(InterestDto::class.java), username = this.pathVariable("username")))
    }
    fun deleteInterestResponse(req: ServerRequest): Mono<ServerResponse> = ServerResponse.accepted().body(
            req.bodyToMono(InterestDto::class.java).filterWhen { webClientService.checkUsernameAsync(req.pathVariable("username")) }
                    .flatMap { concertService.findById(it.concertId) }
                    .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND,"Concert not found")))
                    .flatMap { interestService.deleteByUsernameAndConcertId(concertId = it.id?:"null", username = req.pathVariable("username")) }

    )
}