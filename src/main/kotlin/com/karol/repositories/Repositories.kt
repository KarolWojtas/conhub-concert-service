package com.karol.repositories


import com.karol.domain.Concert
import com.karol.domain.Venue
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ConcertRepository : ReactiveMongoRepository<Concert, String>{
    fun findByName(name: String): Mono<Concert>
    fun findAllByVenueId(venueId: String): Flux<Concert>
}

interface VenueRepository : ReactiveMongoRepository<Venue, String>{
    fun findByName(name: String): Mono<Venue>
}