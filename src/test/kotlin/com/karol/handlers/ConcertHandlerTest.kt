package com.karol.handlers

import com.karol.domain.Concert
import com.karol.domain.Venue
import com.karol.services.ConcertService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.time.ZonedDateTime

class ConcertHandlerTest{

    @Mock lateinit var concertService: ConcertService
    @InjectMocks lateinit var concertHandler: ConcertHandler
    val venue = Venue(id = "idVenue", name = "Venue")
    val venue2 = Venue(id = "idVenue2", name = "Venue2")
    val concert1 = Concert(id = "id1", name = "Concert", venue = venue, date = LocalDateTime.now().plusDays(2L), comments = null)
    val concert2 = Concert(id = "id2", name = "Concert2", venue = venue, date = LocalDateTime.now().plusDays(5L), comments = null)
    val concert3 = Concert(id = "idVenue3", name = "Concert3", venue = venue2, date = LocalDateTime.now().plusDays(3L), comments = null)
    val concert4 = Concert(id = "idVenue4", name = "Concert4", venue = venue2, date = LocalDateTime.now().minusDays(3L), comments = null)
    val allConcerts = listOf(concert1, concert2, concert3, concert4)
    @BeforeEach
    fun beforeEach() = MockitoAnnotations.initMocks(this)

    @Test
    fun `should inject mocks`() = assertNotNull(concertHandler)

    @Test
    fun `should find concerts by name like`(){
        given(concertService.findByNameLike(name = ArgumentMatchers.anyString(), direction = ArgumentMatchers.anyString()
                , by = ArgumentMatchers.anyString(), size = ArgumentMatchers.anyInt(), page = ArgumentMatchers.anyInt())).willReturn(listOf(concert1, concert2).toFlux())

        StepVerifier.create(concertHandler.concertsByNameLikeFlux(name = "con", by = "name", direction = "asc",page = 0, size = 3))
                .expectNext(concert1,concert2)
                .verifyComplete()
        concertHandler.findConcertsByNameResponse(name = "con", by = "name", direction = "asc",page = 0, size = 3).block().also {
            assertEquals(HttpStatus.OK, it?.statusCode())
        }
    }
    @Test
    fun `should filter concerts with params - after and before`(){
        given(concertService.findAll(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).willReturn(allConcerts.toFlux())

        StepVerifier.create(concertHandler.concertWithSearchParamsFlux(name = "name", by = "name", direction = "asc", size = 4, page = 0,
                after = LocalDateTime.now(), before = LocalDateTime.now().plusDays(4), venues = null))
                .expectNext(concert1, concert3)
                .verifyComplete()
    }
    @Test
    fun `should filter concerts with params - venue and after and before`(){
        given(concertService.findAll(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).willReturn(allConcerts.toFlux())

        StepVerifier.create(concertHandler.concertWithSearchParamsFlux(name = "name", by = "name", direction = "asc", size = 4, page = 0,
                after = LocalDateTime.now().minusDays(4L), before = null, venues = listOf("Venue2")))
                .expectNext(concert3, concert4)
                .verifyComplete()
        StepVerifier.create(concertHandler.concertWithSearchParamsFlux(name = "name", by = "name", direction = "desc", page = 0, size = 4,
                after = LocalDateTime.now().plusDays(2L), before = LocalDateTime.now().plusDays(6L), venues = listOf("Venue", "Venue2")))
                .expectNext(concert2, concert3)
                .verifyComplete()

        StepVerifier.create(concertHandler.concertWithSearchParamsFlux(name = "name", by = "name", direction = "desc", page = 0, size = 4,
                after = LocalDateTime.now().plusDays(2L).minusSeconds(5L), before = LocalDateTime.now().plusDays(6L), venues = listOf("Venue", "Venue2")))
                .expectNext(concert1, concert2, concert3)
                .verifyComplete()
    }
    @Test
    fun `should find concerts with params - page and size`(){
        given(concertService.findAll(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).willReturn(allConcerts.toFlux())

        StepVerifier.create(concertHandler.concertWithSearchParamsFlux(name = "name", by = "name", direction = "desc", page = 1, size = 2,
                after = null, before = null, venues = null))
                .expectNext(concert3, concert4)
                .verifyComplete()
        StepVerifier.create(concertHandler.concertWithSearchParamsFlux(name = "name", by = "name", direction = "desc", page = 2, size = 1,
                after = null, before = null, venues = null))
                .expectNext(concert3)
                .verifyComplete()
    }
    @Test
    fun `should return response OK`(){
        given(concertService.findAll(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).willReturn(allConcerts.toFlux())

        concertHandler.findConcertsBySearchParams(name = "name", by = "name", direction = "desc", page = 0, size = 4,
                after = null, before = null, venues = null).block().also {
            assertEquals(HttpStatus.OK, it?.statusCode())
        }
    }
    @Test
    fun `should return error when no concerts found `(){
        given(concertService.findAll(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).willReturn(Flux.empty())

        StepVerifier.create(concertHandler.concertWithSearchParamsFlux(name = "name", by = "name", direction = "desc", page = 0, size = 4,
                after = null, before = null, venues = null))
                .expectError(ResponseStatusException::class.java)
                .verify()

    }

}