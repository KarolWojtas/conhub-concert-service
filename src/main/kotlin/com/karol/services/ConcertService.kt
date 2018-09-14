package com.karol.services

import com.karol.domain.Concert
import com.karol.domain.Venue
import com.karol.repositories.ConcertRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


interface ConcertService {
    fun findById(id: String): Mono<Concert>
    fun findByName(name: String): Mono<Concert>
    fun findAllByVenueId(venueId: String): Flux<Concert>
}

@Service
class ConcertServiceImpl: ConcertService{
    override fun findByName(name: String): Mono<Concert> = concertRepository.findByName(name)

    override fun findAllByVenueId(venueId: String): Flux<Concert>  = concertRepository.findAllByVenueId(venueId)

    override fun findById(id: String): Mono<Concert> = concertRepository.findById(id)

    @Autowired lateinit var concertRepository: ConcertRepository
}