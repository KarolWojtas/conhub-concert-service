package com.karol.routes

import com.karol.ConcertVenueRouterConfig
import com.karol.domain.Concert
import com.karol.domain.Venue
import com.karol.handlers.ConcertHandler
import com.karol.handlers.VenueHandler
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import org.mockito.*
import org.mockito.BDDMockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux
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

    val venue = Venue(id = "idVenue", name = "Venue")
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

}