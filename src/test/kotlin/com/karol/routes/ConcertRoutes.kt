package com.karol.routes

import com.karol.ConcertVenueRouterConfig
import com.karol.domain.Concert
import com.karol.domain.ConcertDto
import com.karol.domain.Venue
import com.karol.handlers.ConcertCommentHandler
import com.karol.handlers.ConcertHandler
import com.karol.handlers.InterestHandler
import com.karol.handlers.VenueHandler
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher
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
class ConcertRoutesTest{
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

    val venue = Venue(id = "idVenue", name = "Venue", avatar = null)
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
    @Test
    fun `should GET concert by id`(){
        val mockRequest = MockServerRequest.builder().pathVariable("concertId", "idC").build()
        val response = ServerResponse.ok().body(concert2.toMono())
        given(concertHandler.findByIdResponse(any(ServerRequest::class.java)?:mockRequest)).willReturn(response)

        webclient.get().uri("/concerts/(concertId)", "idC").accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isOk
                .expectBody().jsonPath("$.id").isEqualTo("id2")
    }
    @Test
    fun `should PATCH concert by id`(){
        val concertDto = ConcertDto(name = "name", date = LocalDateTime.now())
        val mockRequest = MockServerRequest.builder().pathVariable("concertId", "idC").body(concertDto.toMono())
        val response = ServerResponse.accepted().body(concert2.toMono())
        given(concertHandler.patchByIdResponse(any(ServerRequest::class.java)?:mockRequest)).willReturn(response)

        webclient.patch().uri("/concerts/{concertId}", "idC").body(concertDto.toMono()).exchange()
                .expectStatus().isAccepted
                .expectBody()
                .jsonPath("$.id").isEqualTo("id2")
                .jsonPath("$.name").isEqualTo(concert2.name)


    }
    @Test
    fun `should delete by id`(){
        val mockRequest = MockServerRequest.builder().pathVariable("concertId", "idC").build()
        val response = ServerResponse.accepted().body(Mono.empty())
        given(concertHandler.deleteByIdResponse(any(ServerRequest::class.java)?:mockRequest)).willReturn(response)

        webclient.delete().uri("/concerts/{concertId}","idC").exchange()
                .expectStatus().isAccepted
    }
}