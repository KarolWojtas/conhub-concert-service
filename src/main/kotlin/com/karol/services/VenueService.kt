package com.karol.services

import com.karol.domain.Venue
import com.karol.domain.VenueDto
import com.karol.repositories.VenueRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

interface VenueService {
    fun findByName(name: String): Mono<Venue>
    fun findAll(): Flux<Venue>
    fun findById(venueId: String): Mono<Venue>
    fun patchVenue(venueId: String, venueDto: VenueDto): Mono<Venue>
    fun deleteById(venueId: String): Mono<Void>
}
@Service class VenueServiceImpl: VenueService {
    @Autowired lateinit var venueRepository: VenueRepository

    constructor(venueRepository: VenueRepository){
        this.venueRepository = venueRepository
    }

    override fun findByName(name: String) = venueRepository.findByName(name)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND,"Venue not found")))

    override fun findAll(): Flux<Venue> = venueRepository.findAll()

    override fun findById(venueId: String): Mono<Venue>  = venueRepository.findById(venueId).switchIfEmpty(Venue(id=null, name = "null", avatar = null).toMono())

    override fun patchVenue(venueId: String, venueDto: VenueDto): Mono<Venue> =
        venueRepository.findById(venueId).switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found")))
                .map { Venue(id = it.id, avatar = venueDto.avatar?:it.avatar, name = venueDto.name?:it.name) }
                .flatMap{ venueRepository.save(it)}

    override fun deleteById(venueId: String): Mono<Void> = venueRepository.deleteById(venueId)
            .onErrorMap { ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found") }

}