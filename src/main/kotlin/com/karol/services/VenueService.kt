package com.karol.services

import com.karol.domain.Venue
import com.karol.repositories.VenueRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

interface VenueService {
    fun findByName(name: String): Mono<Venue>
}
@Service class VenueServiceImpl: VenueService {
    @Autowired lateinit var venueRepository: VenueRepository

    override fun findByName(name: String) = venueRepository.findByName(name)
            .doOnEach { if(!it.hasValue()) throw ResponseStatusException(HttpStatus.BAD_REQUEST,"Venue not found") }


}