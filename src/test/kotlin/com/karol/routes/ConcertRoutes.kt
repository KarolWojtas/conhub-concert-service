package com.karol.routes

import com.karol.ConcertVenueRouterConfig
import com.karol.domain.Concert
import com.karol.domain.Venue
import com.karol.handlers.ConcertHandler
import com.karol.handlers.VenueHandler
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@WebFluxTest
@ExtendWith(SpringExtension::class)
@Import(ConcertVenueRouterConfig::class)
class ConcertRoutesTest{
    @Autowired
    lateinit var webclient: WebTestClient
    //Muszą być wszystkie beany aby załadować kontekst
    @MockBean
    lateinit var venueHandler: VenueHandler
    @MockBean
    lateinit var concertHandler: ConcertHandler

    val venue = Venue(id = "idVenue", name = "Venue")
    val concert1 = Concert(id = "id1", name = "Concert", venue = venue, date = LocalDateTime.now())
    val concert2 = Concert(id = "id2", name = "Concert2", venue = venue, date = LocalDateTime.now())

    @Test
    fun `should GET concerts based on request params`(){
        given(concertHandler.findConcertsBySearchParams(anyString(), anyString(), anyString(), anyLong(), anyLong(), anyList(),
                any(LocalDateTime::class.java)?: LocalDateTime.now(), any(LocalDateTime::class.java)?: LocalDateTime.now()) ).willReturn(ServerResponse.ok()
                .body(Flux.just(concert1,concert2)))

        webclient.get().uri{it.path("/concerts").queryParam("name","concert").queryParam("by","name").queryParam("direction","asc").queryParam("size","5").queryParam("page","0")
                .queryParam("before","2018-09-16T15:43:25.565").queryParam("after","2018-09-16T15:43:25.565").queryParam("venue","Venue").build()}
                .exchange()
                .expectStatus().isOk
                .expectBody().jsonPath("$.[0].name", Matchers.`is`("Concert"))
    }
}