package com.karol.handlers

import com.karol.domain.Concert
import com.karol.domain.Venue
import com.karol.domain.VenueDto
import com.karol.services.ConcertService
import com.karol.services.VenueService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockMultipartHttpServletRequest
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime



class VenueHandlerUnitTest{
    @Mock lateinit var venueService: VenueService
    @Mock lateinit var concertService: ConcertService
    @Mock lateinit var concertCommentHandler: ConcertCommentHandler
    @InjectMocks lateinit var venueHandler: VenueHandler
    val venue = Venue(id = "idVenue", name = "Venue", avatar = null)
    val concert1 = Concert(id = "id1", name = "Concert", venue = venue, date = LocalDateTime.now())
    val concert2 = Concert(id = "id2", name = "Concert2", venue = venue, date = LocalDateTime.now())
    @BeforeEach
    fun beforeEach() = MockitoAnnotations.initMocks(this)

    @Test
    fun `dependencies injected`(){
        assertNotNull(venueHandler)
    }
    @Test
    fun `should find all concerts by venue name`(){
        given(venueService.findByName(anyString())).willReturn(venue.toMono())
        given(concertService.findAllByVenueId(anyString())).willReturn(Flux.just(concert1,concert2))
        //verify flux that will be in response body
        StepVerifier.create(venueHandler.concertsByVenueNameFlux("Venue"))
                .expectNext(concert1, concert2)
                .verifyComplete()
        //verify response status
        venueHandler.findConcertsByVenueName("Venue").block().also {
            assertEquals(HttpStatus.OK, it?.statusCode())
        }
    }
    @Test
    fun `should find all venues`(){
        given(venueService.findAll()).willReturn(Flux.just(venue))
        venueHandler.findAllResponse().block().also { assertEquals(HttpStatus.OK, it?.statusCode()) }
    }
    @Test
    fun `should get venue by id`(){
        val mockRequest = MockServerRequest.builder().pathVariable("venueId", "venue").build()
        given(venueService.findById(anyString())).willReturn(venue.toMono())

        venueHandler.findById(mockRequest).block().also {
            assertEquals(HttpStatus.OK, it?.statusCode())
        }
    }
    @Test
    fun `should patch by id`(){
        val venueDto =VenueDto(name = "name", avatar = null)
        val mockrequest = MockServerRequest.builder().pathVariable("venueId", "venue").body(venueDto.toMono())
        given(venueService.patchVenue(anyString(), any(VenueDto::class.java)?:venueDto)).willReturn(venue.toMono())

        venueHandler.patchById(mockrequest).block().also {
            assertEquals(HttpStatus.ACCEPTED, it?.statusCode() )
        }
    }
    @Test
    fun `should delete by id`(){
        given(venueService.deleteById(anyString())).willReturn(Mono.empty())
        val mockRequest = MockServerRequest.builder().pathVariable("venueId","venue").build()

        venueHandler.deleteById(mockRequest).block().also {
            assertEquals(HttpStatus.ACCEPTED, it?.statusCode() )
        }
    }



}