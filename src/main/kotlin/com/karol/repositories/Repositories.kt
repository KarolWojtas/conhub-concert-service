package com.karol.repositories


import com.karol.domain.Concert
import com.karol.domain.ConcertComment
import com.karol.domain.Interest
import com.karol.domain.Venue
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.query.Param
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ConcertRepository : ReactiveMongoRepository<Concert, String>{
    fun findByName(name: String): Mono<Concert>
    fun findAllByVenueId(venueId: String): Flux<Concert>
    fun findAllByNameLikeIgnoreCase(name: String, pageable: Pageable): Flux<Concert>
    fun findAllByNameLikeIgnoreCase(name: String, sort: Sort): Flux<Concert>
}

interface VenueRepository : ReactiveMongoRepository<Venue, String>{
    fun findByName(name: String): Mono<Venue>
}
interface ConcertCommentRepository : ReactiveMongoRepository<ConcertComment, String>{
    fun findAllByConcertId(concertId: String, sort: Sort): Flux<ConcertComment>
}
interface InterestRespository : ReactiveMongoRepository<Interest, String>{
    fun findAllByUsername(username: String): Flux<Interest>
    fun findAllByConcertId(concertId: String): Flux<Interest>
    fun findAllByUsernameAndConcertId(@Param("username") username: String,@Param("concertId") concertId: String): Mono<Interest>
    fun deleteByUsernameAndConcertId(@Param("username") username: String,@Param("concertId") concertId: String): Mono<Void>
}