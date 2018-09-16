package com.karol.services

import com.karol.domain.Concert
import com.karol.domain.Venue
import com.karol.repositories.ConcertRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.function.Executable
import org.mockito.*
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.domain.Sort
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.time.LocalDateTime
import java.time.ZonedDateTime


@ExtendWith(SpringExtension::class)
@DataMongoTest
class ConcertServiceIntTest{


}



class ConcertServiceUnitTest{
    @Mock lateinit var concertRepository: ConcertRepository
    @InjectMocks lateinit var concertService: ConcertServiceImpl
    val venue = Venue(id = "idVenue", name = "Venue")
    val venue2 = Venue(id = "idVenue2", name = "venue2")
    val concert1 = Concert(id = "id1", name = "Concert", venue = venue, date = LocalDateTime.now())
    val concert2 = Concert(id = "id2", name = "Concert2", venue = venue, date = LocalDateTime.now())
    val concert3 = Concert(id = "id3", name = "Concert3", venue = venue2, date = LocalDateTime.now())

    @BeforeEach
    fun setUp(){
        MockitoAnnotations.initMocks(this)
    }
    @Test
    fun `dependencies injected`(){
        assertNotNull(concertService)
    }
    @Test
    fun `should find concert by name`(){
        given(concertRepository.findByName(anyString())).willReturn(Mono.just(concert1))

        concertService.findByName("Concert").block().also {
            assertAll("concert",
                    Executable { assertEquals(concert1.id, it?.id) },
                    Executable { assertEquals(concert1.name, it?.name) }
            )
        }


    }
    @Test
    fun `should return all concerts by venue id`(){
        given(concertRepository.findAllByVenueId(anyString())).willReturn(Flux.just(concert1,concert2))

         concertService.findAllByVenueId("idVenue").toIterable().toList().also {
             assertAll("concert list",
                     Executable { assertEquals(2,it.size) },
                     Executable { assertEquals(concert1, it[0]) },
                     Executable { assertEquals(concert2, it[1]) }
             )
         }


    }
    @Test
    fun `should find concert by id`(){
        given(concertRepository.findById(anyString())).willReturn(concert1.toMono())

        concertService.findById("id").block().also {
            assertEquals(concert1, it)
        }
    }
    @Test
    fun `should find concerts with name query specified`(){
        given(concertRepository.findAllByNameLikeIgnoreCase(ArgumentMatchers.anyString(), ArgumentMatchers.any(Sort::class.java)?: Sort.by("name")))
                .willReturn(Flux.just(concert1,concert2,concert3))

        val concertList = concertService.findAll(name = "con", by = "name", direction = "asc" )

        assertNotNull(concertList)
        Mockito.verify(concertRepository, Mockito.times(1)).findAllByNameLikeIgnoreCase(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(Sort::class.java)?: Sort.by("name"))
    }
    @Test
    fun `should find all concerts if name query is null`(){
        given(concertRepository.findAll( ArgumentMatchers.any(Sort::class.java)?: Sort.by("name")))
                .willReturn(Flux.just(concert1,concert2,concert3))

        val concertList = concertService.findAll(by = "name", direction = "asc", name = null )

        assertNotNull(concertList)
        Mockito.verify(concertRepository, Mockito.times(1)).findAll(ArgumentMatchers.any(Sort::class.java)?: Sort.by("name"))
    }


}