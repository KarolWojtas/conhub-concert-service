package com.karol.routes

import com.karol.ConcertVenueRouterConfig
import com.karol.domain.Concert
import com.karol.domain.Venue
import com.karol.domain.VenueDto
import com.karol.handlers.ConcertCommentHandler
import com.karol.handlers.ConcertHandler
import com.karol.handlers.VenueHandler
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import org.mockito.*
import org.mockito.BDDMockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.body
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.time.LocalDateTime


@WebFluxTest
@ExtendWith(SpringExtension::class)
@Import(ConcertVenueRouterConfig::class)
class VenueRouteTest{
    @Autowired lateinit var webclient: WebTestClient
    //Muszą być wszystkie beany aby załadować kontekst
    @MockBean
    lateinit var venueHandler: VenueHandler
    @MockBean
    lateinit var concertHandler: ConcertHandler
    @MockBean
    lateinit var concertCommentHandler: ConcertCommentHandler

    val venue = Venue(id = "idVenue", name = "Venue", avatar = null)
    val concert1 = Concert(id = "id1", name = "Concert", venue = venue, date = LocalDateTime.now())
    val concert2 = Concert(id = "id2", name = "Concert2", venue = venue, date = LocalDateTime.now())

    @Test
    fun `dependencies injected`() {
        Assert.assertNotNull(venueHandler)
    }

    @Test
    fun `should GET concerts in venue`(){
        given(venueHandler.findConcertsByVenueName(ArgumentMatchers.anyString())).willReturn(ServerResponse.ok().body( Flux.just(concert1,concert2)))

        webclient.get().uri("/venues/{venueName}/concerts", "Venue").exchange().expectStatus().isOk
    }
    @Test
    fun `should GET all venues`(){
        given(venueHandler.findAllResponse()).willReturn(ServerResponse.ok().body(Flux.just(venue)))

        webclient.get().uri("/venues").exchange()
                .expectStatus().isOk
                .expectBodyList<Venue>().hasSize(1).contains(venue)
    }
    @Test
    fun `should GET by id`(){
        val mockRequest: MockServerRequest = MockServerRequest.builder().pathVariable("venueId", "idV").build()
        val response = ServerResponse.ok().body(venue.toMono()).toMono()
        given(venueHandler.findById(any(ServerRequest::class.java)?:mockRequest)).willReturn(response)

        webclient.get().uri("/venues/{venueId}", "venue").accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isOk
                .expectBody(Venue::class.java)
    }
    @Test
    fun `should PATCH by id`(){
        val venueDto = VenueDto(name = "name", avatar = null)
        val mockRequest: MockServerRequest = MockServerRequest.builder().pathVariable("venueId", "idV").body(venueDto.toMono())
        val response = ServerResponse.accepted().body(venue.toMono()).toMono()
        given(venueHandler.patchById(any(ServerRequest::class.java)?:mockRequest)).willReturn(response.toMono())

        webclient.patch().uri("/venues/{venueId}", "venueId").body(venueDto.toMono()).accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isAccepted
                .expectBody(Venue::class.java)
    }
    @Test
    fun `should DELETE by id`(){
        val mockRequest: MockServerRequest = MockServerRequest.builder().pathVariable("venueId", "idV").build()
        val response = ServerResponse.accepted().body(Mono.empty()).toMono()
        given(venueHandler.deleteById(any(ServerRequest::class.java)?:mockRequest)).willReturn(response)

        webclient.delete().uri("/venues/{venueId}", "venueId").exchange()
                .expectStatus().isAccepted
                .expectBody(Unit::class.java)
    }

}