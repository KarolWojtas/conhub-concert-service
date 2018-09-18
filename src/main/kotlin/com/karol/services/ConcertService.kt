package com.karol.services

import com.karol.domain.Concert
import com.karol.domain.ConcertDto
import com.karol.domain.Venue
import com.karol.domain.VenueDto
import com.karol.repositories.ConcertRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono



interface ConcertService {
    fun findById(id: String): Mono<Concert>
    fun findByName(name: String): Mono<Concert>
    fun findAllByVenueId(venueId: String): Flux<Concert>
    fun findByNameLike(name: String, by: String?, direction: String?, size: Int?, page: Int?): Flux<Concert>
    fun findAll(name: String?, by: String?, direction: String?): Flux<Concert>
    fun patchById(concertId: String, concertDto: ConcertDto, venue: Venue?): Mono<Concert>
    fun deleteById(concertId: String): Mono<Void>
    fun findAllByIds(concertIds: Flux<String>): Flux<Concert>

}

@Service
class ConcertServiceImpl: ConcertService{
    @Autowired lateinit var concertRepository: ConcertRepository

    constructor(concertRepository: ConcertRepository){
        this.concertRepository = concertRepository
    }

    override fun findByNameLike(name: String, by: String?, direction: String?, size: Int?, page: Int?): Flux<Concert> {
        val sortDirection = if(direction?:"asc" == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        val sortObject = Sort.by(sortDirection, by?:"name")
        return concertRepository.findAllByNameLikeIgnoreCase(name = name, pageable = PageRequest.of(page?:0, size?:5, sortObject))

    }
    override fun findByName(name: String): Mono<Concert> = concertRepository.findByName(name)

    override fun findAllByVenueId(venueId: String): Flux<Concert>  = concertRepository.findAllByVenueId(venueId)

    override fun findById(id: String): Mono<Concert> = concertRepository.findById(id)//.switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Concert not found")))

    override fun findAll(name: String?, by: String?, direction: String?): Flux<Concert> {
        val sortDirection = if(direction?:"asc" == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        val sortObject = Sort.by(sortDirection, by?:"name")
        return if(name != null) concertRepository.findAllByNameLikeIgnoreCase(name = name, sort = sortObject )
        else concertRepository.findAll(sortObject)
    }

    override fun patchById(concertId: String, concertDto: ConcertDto, venue: Venue?): Mono<Concert> = concertRepository.findById(concertId)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND,"Concert not found")))
            .map { Concert(id = it.id, name = concertDto.name?:it.name, date = concertDto.date?:it.date, venue = venue?:it.venue) }
            .flatMap { concertRepository.save(it) }

    override fun deleteById(concertId: String): Mono<Void> = concertRepository.findById(concertId).flatMap { concertRepository.delete(it) }

    override fun findAllByIds(concertIds: Flux<String>): Flux<Concert> {
        return concertRepository.findAllById(concertIds)
    }
}