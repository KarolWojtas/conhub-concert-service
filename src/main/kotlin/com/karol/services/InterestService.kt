package com.karol.services

import com.karol.domain.Interest
import com.karol.repositories.InterestRespository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface InterestService{
    fun findAllByUsername(username: String): Flux<Interest>
    fun findAllByConcertId(concertId: String): Flux<Interest>
    fun postInterest(interest: Interest): Mono<Interest>
    fun deleteById(interestId: String): Mono<Void>
    fun deleteByUsernameAndConcertId(concertId: String, username: String): Mono<Void>
    fun findByUsernameAndConcertId(username: String, concertId: String): Mono<Interest>
}
@Service
class InterestServiceImpl: InterestService{
    @Autowired
    lateinit var interestRepository: InterestRespository

    override fun findAllByUsername(username: String): Flux<Interest> = interestRepository.findAllByUsername(username = username)

    override fun findAllByConcertId(concertId: String): Flux<Interest> = interestRepository.findAllByConcertId(concertId = concertId)

    override fun postInterest(interest: Interest): Mono<Interest> = interestRepository.save(interest)

    override fun deleteById(interestId: String) = interestRepository.deleteById(interestId)

    override fun deleteByUsernameAndConcertId(concertId: String, username: String) = interestRepository.findAllByUsernameAndConcertId(username = username, concertId = concertId)
            .flatMap { interestRepository.deleteById(it.id?:"") }

    override fun findByUsernameAndConcertId(username: String, concertId: String) = interestRepository.findAllByUsernameAndConcertId(username = username, concertId = concertId)

}