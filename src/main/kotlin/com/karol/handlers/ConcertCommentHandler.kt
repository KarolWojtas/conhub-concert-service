package com.karol.handlers

import com.karol.domain.ConcertComment
import com.karol.domain.ConcertCommentDto
import com.karol.domain.UserDetails
import com.karol.services.ConcertCommentService
import com.karol.services.ConcertService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class ConcertCommentHandler{
    @Autowired
    lateinit var concertService: ConcertService
    @Autowired
    lateinit var concertCommentService: ConcertCommentService
    @Qualifier("loadBalancedWebClientBuilder")
    @Autowired lateinit var webClientBuilder: WebClient.Builder
    val DEFAUTL_PAGE = 0L
    val DEFAULT_SIZE = 10L

    fun commentsByConcertFlux(page: Long?, size: Long?, by: String?, direction: String?, concertId: String): Flux<ConcertComment> =
        concertCommentService.findAllByConcertId(concertId= concertId, by = by, direction = direction)
                .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND,"No comments")))
                .skip((page?:DEFAUTL_PAGE)*(size?:DEFAULT_SIZE))
                .take(size?:DEFAULT_SIZE)



    fun findAllByConcertIdResponse(request: ServerRequest): Mono<ServerResponse> {
        with(request){
            val concertId: String = pathVariable("concertId")
            val by: String? = queryParam("by").orElse(null)
            val direction: String? = queryParam("direction").orElse(null)
            val page: String? = queryParam("page").orElse(null)
            val size: String? = queryParam("size").orElse(null)

            return ServerResponse.ok().body(concertService.findById(concertId)
                    .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Concert not found")))
                    .flatMapMany { commentsByConcertFlux(concertId = it?.id?:"", page = page?.toLong(), size = size?.toLong(), by = by, direction = direction)})
        }

    }
    fun saveCommentDtoResponse(request: ServerRequest): Mono<ServerResponse> = with(request){
        ServerResponse.ok().body(saveCommentMono(username = pathVariable("username"), concertId = pathVariable("concertId"),
                comment = this.bodyToMono(ConcertCommentDto::class.java)))
    }
    fun saveCommentMono(comment: Mono<ConcertCommentDto>, concertId: String, username: String): Mono<ConcertComment> {

        return concertService.findById(concertId)
                .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Concert not found")))
                .filterWhen { checkUsernameAsync(username) }
                .zipWith(comment)
                .map { ConcertComment(id = null, text = it.t2.text, timestamp = it.t2.timestamp?: LocalDateTime.now(), concert = it.t1, username = username) }
                .flatMap(concertCommentService::saveComment)
                .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Save unsuccessful")))
    }

    //This crazy!!! with filterWhen
    fun checkUsernameAsync(username: String) = webClientBuilder.build().get().uri("http://conhub-user-service/{username}", username).accept(MediaType.APPLICATION_JSON)
            .retrieve().onStatus(HttpStatus::is4xxClientError){Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))}
                       .onStatus(HttpStatus::is5xxServerError){Mono.error(ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User service internal error"))}
                       .bodyToMono(UserDetails::class.java).map { it != null }



}