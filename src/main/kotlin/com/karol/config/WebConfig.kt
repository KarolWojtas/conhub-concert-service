package com.karol.config

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced

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
}