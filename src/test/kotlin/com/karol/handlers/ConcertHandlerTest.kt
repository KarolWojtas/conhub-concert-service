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
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import reactor.test.StepVerifier

class ConcertHandlerTest{

    @Mock lateinit var concertService: ConcertService
    @InjectMocks lateinit var concertHandler: ConcertHandler
    val venue = Venue(id = "idVenue", name = "Venue")
    val concert1 = Concert(id = "id1", name = "Concert", venue = venue)
    val concert2 = Concert(id = "id2", name = "Concert2", venue = venue)
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

}