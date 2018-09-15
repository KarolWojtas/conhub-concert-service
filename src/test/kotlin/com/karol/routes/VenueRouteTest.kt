package com.karol.routes

import com.karol.RouterConfig
import com.karol.domain.Concert
import com.karol.domain.Venue
import com.karol.handlers.ConcertHandler
import com.karol.handlers.VenueHandler
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.BDDMockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux
import reactor.core.publisher.toMono


@WebFluxTest
@ExtendWith(SpringExtension::class)
@Import(RouterConfig::class)
class VenueRouteTest{
    @Autowired lateinit var webclient: WebTestClient
    @MockBean
    lateinit var venueHandler: VenueHandler
    @MockBean
    lateinit var concertHandler: ConcertHandler

    val venue = Venue(id = "idVenue", name = "Venue")
    val concert1 = Concert(id = "id1", name = "Concert", venue = venue)
    val concert2 = Concert(id = "id2", name = "Concert2", venue = venue)

    @Test
    fun `dependencies injected`() {
        Assert.assertNotNull(venueHandler)
    }

    @Test
    fun `should GET concerts in venue`(){
        given(venueHandler.findConcertsByVenueName(ArgumentMatchers.anyString())).willReturn(ServerResponse.ok().body( Flux.just(concert1,concert2)))

        webclient.get().uri("/venues/{venueName}/concerts", "Venue").exchange().expectStatus().isOk
    }

}