package com.karol.services

import com.karol.domain.Concert
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
}

@Service
class ConcertServiceImpl: ConcertService{
    override fun findByNameLike(name: String, by: String?, direction: String?, size: Int?, page: Int?): Flux<Concert> {
        val sortDirection = if(direction?:"asc" == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        val sortObject = Sort.by(sortDirection, by?:"name")
        return concertRepository.findAllByNameLikeIgnoreCase(name = name, pageable = PageRequest.of(page?:0, size?:5, sortObject))

    }
    override fun findByName(name: String): Mono<Concert> = concertRepository.findByName(name)

    override fun findAllByVenueId(venueId: String): Flux<Concert>  = concertRepository.findAllByVenueId(venueId)

    override fun findById(id: String): Mono<Concert> = concertRepository.findById(id)

    override fun findAll(name: String?, by: String?, direction: String?): Flux<Concert> {
        val sortDirection = if(direction?:"asc" == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        val sortObject = Sort.by(sortDirection, by?:"name")
        return if(name != null) concertRepository.findAllByNameLikeIgnoreCase(name = name, sort = sortObject )
        else concertRepository.findAll(sortObject)
    }

    @Autowired lateinit var concertRepository: ConcertRepository

    constructor(concertRepository: ConcertRepository){
        this.concertRepository = concertRepository
    }
}