package com.karol.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.karol.domain.ConcertComment
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.stereotype.Component
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import reactor.core.publisher.UnicastProcessor
import java.time.Duration
import java.util.function.Consumer

@EnableReactiveMongoRepositories
class MongoConfig: AbstractReactiveMongoConfiguration(){
    override fun reactiveMongoClient(): MongoClient = MongoClients.create()

    override fun getDatabaseName(): String = "concert"

}
@Configuration
class WebConfig{
    @Bean
    @LoadBalanced
    fun loadBalancedWebClientBuilder(): WebClient.Builder {
        return WebClient.builder()
    }
    @Bean
    fun webSocketHandlerMapping(webSocketHandler: WebSocketHandler) = SimpleUrlHandlerMapping().apply {
            order = 1
            urlMap = hashMapOf("/comments-emitter" to webSocketHandler)
        }
    @Bean
    fun webSocketHandlerAdapter() = WebSocketHandlerAdapter()
    @Bean
    fun webSocketHandler(commentFlux: Flux<ConcertComment>, objectMapper: ObjectMapper):WebSocketHandler = WebSocketHandler {session ->
        session.send(commentFlux.map { session.textMessage(objectMapper.writeValueAsString(it)) })
    }
    @Bean
    fun commentPublisher() = UnicastProcessor.create<ConcertComment>()
    @Bean
    fun commentFlux(commentPublisher: UnicastProcessor<ConcertComment>): Flux<ConcertComment> = commentPublisher.share()
}

