package com.karol.routes

import com.karol.ConcertVenueRouterConfig
import com.karol.domain.Concert
import com.karol.domain.Interest
import com.karol.domain.InterestDto
import com.karol.domain.Venue
import com.karol.handlers.ConcertCommentHandler
import com.karol.handlers.ConcertHandler
import com.karol.handlers.InterestHandler
import com.karol.handlers.VenueHandler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
class InterestRoutesTest{
    @Autowired
    lateinit var webclient: WebTestClient
    //Muszą być wszystkie beany aby załadować kontekst
    @MockBean
    lateinit var venueHandler: VenueHandler
    @MockBean
    lateinit var concertHandler: ConcertHandler
    @MockBean
    lateinit var concertCommentHandler: ConcertCommentHandler
    @MockBean
    lateinit var interestHandler: InterestHandler
    val mockServerRequest = MockServerRequest.builder().build()
    val venue = Venue(id = "idVenue", name = "Venue", avatar = null)
    val venue2 = Venue(id = "idVenue2", name = "Venue2", avatar = null)
    val concert1 = Concert(id = "id1", name = "Concert", venue = venue, date = LocalDateTime.now().plusDays(2L))
    val concert2 = Concert(id = "id2", name = "Concert2", venue = venue, date = LocalDateTime.now().plusDays(5L))
    val concert3 = Concert(id = "idVenue3", name = "Concert3", venue = venue2, date = LocalDateTime.now().plusDays(3L))
    val interest1 = Interest(id = "idI", username = "username", concertId = "id1")
    val interestDto = InterestDto(concertId = "idC")

    @Test
    fun `should GET concerts by user`(){
        val serverResponse = ServerResponse.ok().body(Flux.just(concert1, concert2, concert3))
        given(interestHandler.findConcertsByUsernameIntrestsResponse(any(ServerRequest::class.java)?:mockServerRequest)).willReturn(serverResponse)

        webclient.get().uri("/interests/{username}", "username").accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isOk
                .expectBodyList(Concert::class.java)
    }
    @Test
    fun `should post an interest by user`(){
        val serverResponse = ServerResponse.accepted().body(interest1.toMono())
        given(interestHandler.postInterestResponse(any(ServerRequest::class.java)?:mockServerRequest)).willReturn(serverResponse)

        webclient.post().uri("/interests/{username}", "username").body(interestDto.toMono()).exchange()
                .expectStatus().isAccepted
                .expectBody().jsonPath("$.id").isEqualTo("idI")
    }
    @Test
    fun `should DELETE interest by username`(){
        val serverResponse = ServerResponse.accepted().body(Mono.empty())
        given(interestHandler.deleteInterestResponse(any(ServerRequest::class.java)?:mockServerRequest)).willReturn(serverResponse)

        webclient.delete().uri("/interests/{username}", "username").accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isAccepted
    }
}