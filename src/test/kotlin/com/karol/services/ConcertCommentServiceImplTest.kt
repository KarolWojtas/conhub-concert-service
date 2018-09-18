package com.karol.services

import com.karol.domain.Concert
import com.karol.domain.ConcertComment
import com.karol.domain.Venue
import com.karol.repositories.ConcertCommentRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.Sort
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.LocalDateTime

class ConcertCommentServiceImplTest{
    @Mock
    lateinit var concertCommentRepository: ConcertCommentRepository
    @InjectMocks
    lateinit var concertCommentService: ConcertCommentServiceImpl
    val concert1 = Concert(id = "idC", name = "concert1", date = LocalDateTime.now().plusDays(1L), venue = Venue(id = "idV", name = "Venue", avatar = null))
    val comment1 = ConcertComment(id = "idCom", timestamp = LocalDateTime.now(), concert = concert1, text = "text", username = "username")
    val comment2 = ConcertComment(id = "idCom2", timestamp = LocalDateTime.now(), concert = concert1, text = "text2", username = "username")
    @BeforeEach
    fun beforeEach(){
        MockitoAnnotations.initMocks(this)
    }
    @Test
    fun `dependencies injected`() = assertNotNull(concertCommentService)

    @Test
    fun `find All by concert id`(){
        given(concertCommentRepository.findAllByConcertId(anyString(), any(Sort::class.java)?:Sort.by("timestamp"))).willReturn(Flux.just(comment1, comment2))

        StepVerifier.create(concertCommentService.findAllByConcertId(concertId = "idC", direction = "desc", by = "timestamp"))
                .expectNext(comment1, comment2)
                .verifyComplete()
    }

}