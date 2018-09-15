package com.karol.services

import com.karol.domain.Concert
import com.karol.domain.Venue
import com.karol.repositories.ConcertRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.util.stream.Stream


@ExtendWith(SpringExtension::class)
@DataMongoTest

class ConcertServiceIntTest{
    @Autowired lateinit var mongo : MongoTemplate
    @Autowired lateinit var concertRepository: ConcertRepository
    lateinit var concertService: ConcertService
    val venue = Venue(name="venue")
    val concert1 = Concert(name = "concert 1 in the jungle", venue = venue)
    val concert2 = Concert(name = "concert 2 in the jungle", venue = venue)
    val concert3 = Concert(name = "concert 3 in the jungle", venue = venue)

    @BeforeEach
    fun setUp(){
        concertService = ConcertServiceImpl(concertRepository)
        mongo.save(venue)
        mongo.save(concert1)
        mongo.save(concert2)
        mongo.save(concert3)
    }


    @TestFactory
    fun `should find concert by regex dynamic`(): Stream<DynamicTest> =
            Stream.of("cert", "con", "e jun").map { DynamicTest.dynamicTest("test for $it"){
                val concertList: List<Concert>? = concertService.findByNameLike(name = it, by = "name", direction = "desc", size = 3, page = 0)?.collectList().block()

                assertNotNull(concertList)
                if (concertList != null){
                    assertEquals(3, concertList.size)
                }
            } }
    @Test
    fun `should use default values if null`(){
        val concertList = concertService.findByNameLike("con", "name", "desc", null,null).collectList().block()
        if(concertList!=null){
            assert(concertList.size > 0)
        }
    }
}



class ConcertServiceUnitTest{
    @Mock lateinit var concertRepository: ConcertRepository
    @InjectMocks lateinit var concertService: ConcertServiceImpl
    val venue = Venue(id = "idVenue", name = "Venue")
    val concert1 = Concert(id = "id1", name = "Concert", venue = venue)
    val concert2 = Concert(id = "id2", name = "Concert2", venue = venue)

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


}