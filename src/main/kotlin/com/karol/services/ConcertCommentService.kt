package com.karol.services

import com.karol.domain.ConcertComment
import com.karol.repositories.ConcertCommentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ConcertCommentService{
    fun findAllByConcertId(concertId: String, by: String?, direction: String?): Flux<ConcertComment>
    fun saveComment(comment: ConcertComment): Mono<ConcertComment>
}
@Service
class ConcertCommentServiceImpl : ConcertCommentService{
    @Autowired
    lateinit var concertCommentRepository: ConcertCommentRepository
    override fun findAllByConcertId(concertId: String, by: String?, direction: String?): Flux<ConcertComment> {
        val sortDirection = if(direction?:"desc" == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        val sortObject = Sort.by(sortDirection, by?:"timestamp")
        return concertCommentRepository.findAllByConcertId(concertId, sortObject)
    }

    override fun saveComment(comment: ConcertComment): Mono<ConcertComment> = concertCommentRepository.save(comment)

}