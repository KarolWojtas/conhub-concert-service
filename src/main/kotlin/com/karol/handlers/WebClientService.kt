package com.karol.handlers

import com.karol.domain.UserDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Service
class WebClientService{
    @Qualifier("loadBalancedWebClientBuilder")
    @Autowired
    lateinit var webClientBuilder: WebClient.Builder

    fun checkUsernameAsync(username: String) = webClientBuilder.build().get().uri("http://conhub-user-service/{username}", username).accept(MediaType.APPLICATION_JSON)
            .retrieve().onStatus(HttpStatus::is4xxClientError){ Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))}
            .onStatus(HttpStatus::is5xxServerError){ Mono.error(ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User service internal error"))}
            .bodyToMono(UserDetails::class.java).map { it != null }
}