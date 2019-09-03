package com.karol.handlers

import com.karol.domain.Concert
import com.karol.domain.Interest
import com.karol.domain.InterestDto
import com.karol.domain.Venue
import com.karol.services.ConcertService
import com.karol.services.InterestService
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.*
import org.mockito.BDDMockito.*
import org.springframework.http.HttpStatus
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class InterestHandlerTest{
    @Mock
    lateinit var interestService: InterestService
    @Mock
    lateinit var concertService: ConcertService
    @Mock
    lateinit var webClientService: WebClientService
    @Mock lateinit var concertCommentHandler: ConcertCommentHandler
    @InjectMocks
    lateinit var interestHandler: InterestHandler
    val venue = Venue(id = "idVenue", name = "Venue", avatar = null)
    val venue2 = Venue(id = "idVenue2", name = "Venue2", avatar = null)
    val concert1 = Concert(id = "id1", name = "Concert", venue = venue, date = LocalDateTime.now().plusDays(2L))
    val concert2 = Concert(id = "id2", name = "Concert2", venue = venue, date = LocalDateTime.now().plusDays(5L))
    val concert3 = Concert(id = "idVenue3", name = "Concert3", venue = venue2, date = LocalDateTime.now().plusDays(3L))
    val interest1 = Interest(id = "idI", username = "username", concertId = "id1")
    val interest2 = Interest(id = "id2", username = "username", concertId = "id2")
    val interestDto = InterestDto(concertId = "idC")

    @BeforeEach
    fun beforeEach() = MockitoAnnotations.initMocks(this)
    @Test
    fun `dependencies injected`() = assertNotNull(interestHandler)

    @Test
    fun `should find concerts by username interest`(){

        given(interestService.findAllByUsername(anyString())).willReturn(Flux.just(interest1, interest2))
        given(webClientService.checkUsernameAsync(anyString())).willReturn(true.toMono())
        given(concertService.findById(ArgumentMatchers.anyString())).willReturn(Mono.just(concert1)).willReturn(concert2.toMono()).willReturn(Mono.just(concert1)).willReturn(concert2.toMono())
        val mockRequest = MockServerRequest.builder().pathVariable("username", "username").build()

        StepVerifier.create(interestHandler.concertsByUsernameFlux("username"))
                .expectNext(concert1, concert2).verifyComplete()
        interestHandler.findConcertsByUsernameIntrestsResponse(mockRequest).block().also {
            assertEquals(HttpStatus.OK, it?.statusCode())
        }
    }
    @Test
    fun `should post interest by params`(){
        given(concertService.findById(anyString())).willReturn(concert1.toMono())
        given(webClientService.checkUsernameAsync(anyString())).willReturn(true.toMono())
        given(interestService.postInterest(any(Interest::class.java)?:interest1)).willReturn(interest1.toMono())

        val mockrequest = MockServerRequest.builder().pathVariable("username", "username").body(interestDto.toMono())

        StepVerifier.create(interestHandler.postInterestMono(interestDto = interestDto.toMono(), username = "username"))
                .expectNext(interest1).verifyComplete()
        interestHandler.postInterestResponse(mockrequest).block().also {
            assertEquals(HttpStatus.ACCEPTED, it?.statusCode())
        }
    }
    @Test
    fun `should delete by params`(){
        given(concertService.findById(anyString())).willReturn(concert1.toMono())
        given(webClientService.checkUsernameAsync(anyString())).willReturn(true.toMono())
        given(interestService.deleteByUsernameAndConcertId(anyString(), anyString())).willReturn(Mono.empty())
        val mockRequest = MockServerRequest.builder().pathVariable("username", "username").body(interestDto.toMono())

        interestHandler.deleteInterestResponse(mockRequest).block().also {
            assertEquals(HttpStatus.ACCEPTED, it?.statusCode())
        }
    }
}