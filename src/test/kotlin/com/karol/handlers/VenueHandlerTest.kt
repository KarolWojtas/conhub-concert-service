package com.karol.handlers

import com.karol.domain.Concert
import com.karol.domain.Venue
import com.karol.services.ConcertService
import com.karol.services.VenueService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.toMono
import reactor.test.StepVerifier


class VenueHandlerUnitTest{
    @Mock lateinit var venueService: VenueService
    @Mock lateinit var concertService: ConcertService
    @InjectMocks lateinit var venueHandler: VenueHandler
    val venue = Venue(id = "idVenue", name = "Venue")
    val concert1 = Concert(id = "id1", name = "Concert", venue = venue)
    val concert2 = Concert(id = "id2", name = "Concert2", venue = venue)
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


}