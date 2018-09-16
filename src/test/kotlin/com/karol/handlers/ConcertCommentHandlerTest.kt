package com.karol.handlers

import com.karol.domain.Concert
import com.karol.domain.ConcertComment
import com.karol.domain.Venue
import com.karol.services.ConcertCommentService
import com.karol.services.ConcertService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.*
import org.mockito.BDDMockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.URI
import java.time.LocalDateTime

class ConcertCommentHandlerTest{
    @Mock
    lateinit var concertService: ConcertService
    @Mock
    lateinit var concertCommentService: ConcertCommentService
    @Mock
    lateinit var webClientBuilder: WebClient.Builder
    @InjectMocks
    lateinit var concertCommentHandler: ConcertCommentHandler
    val concert1 = Concert(id = "idC", name = "concert1", date = LocalDateTime.now().plusDays(1L), venue = Venue(id = "idV", name = "Venue"))
    val comment1 = ConcertComment(id = "idCom", timestamp = LocalDateTime.now(), concert = concert1, text = "text", username = "username")
    val comment2 = ConcertComment(id = "idCom2", timestamp = LocalDateTime.now(), concert = concert1, text = "text2", username = "username")
    @BeforeEach
    fun beforeEach() = MockitoAnnotations.initMocks(this)

    @Test
    fun `should page concerts`(){
        given(concertCommentService.findAllByConcertId(anyString(), anyString(), anyString())).willReturn(Flux.just(comment1, comment2))

        //RETURN ALL
        StepVerifier.create(concertCommentHandler.commentsByConcertFlux(page = 0, size = 2, direction = "desc", by = "timestamp", concertId = "idC"))
                .expectNext(comment1, comment2)
                .verifyComplete()
        //RETURN FIRST PAGE
        StepVerifier.create(concertCommentHandler.commentsByConcertFlux(page = 0, size = 1, direction = "desc", by = "timestamp", concertId = "idC"))
                .expectNext(comment1)
                .verifyComplete()
        //RETURN LAST PAGE
        StepVerifier.create(concertCommentHandler.commentsByConcertFlux(page = 1, size = 1, direction = "desc", by = "timestamp", concertId = "idC"))
                .expectNext(comment2)
                .verifyComplete()
        //USE DEFAULTS
        StepVerifier.create(concertCommentHandler.commentsByConcertFlux(page= null, size = null, direction = "desc", by = "timestamp", concertId = "idC"))
                .expectNext(comment1, comment2)
                .verifyComplete()
    }
    //TODO nie wiem jak to przetestować
    @Test
    fun `should invoke function with params from request`(){
        given(concertService.findById(anyString())).willReturn(Mono.just(concert1))
        given(concertCommentService.findAllByConcertId(anyString(), anyString(), anyString())).willReturn(Flux.just(comment1, comment2))
        val byCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val directionCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val concertIdCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val mockRequest: MockServerRequest = MockServerRequest.builder().pathVariable("concertId","idC").queryParam("page", "0").queryParam("size", "2").queryParam("direction", "desc")
                .queryParam("by","timestamp").build()

        concertCommentHandler.findAllByConcertIdResponse(mockRequest).block().apply {
            assertEquals(HttpStatus.OK, this?.statusCode())
        }

    }
    @Test
    fun `should save comment`(){
        given(concertService)
    }


}