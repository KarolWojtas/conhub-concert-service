package com.karol.services

import com.karol.domain.Venue
import com.karol.domain.VenueDto
import com.karol.repositories.VenueRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.test.StepVerifier

class VenueServiceUnitTest{
    @Mock lateinit var venueRepository: VenueRepository
    @InjectMocks lateinit var venueService: VenueServiceImpl
    val venue1 = Venue(id="id1", name = "Venue1", avatar = null)
    val venue2 = Venue(id = "id2", name = "Venue2", avatar = null)
    @BeforeEach
    fun beforeEach(){
        MockitoAnnotations.initMocks(this)
    }
    @Test
    fun `dependencies injected`(){
        assertNotNull(venueService)
    }
    @Test
    fun `should find venue by name`(){
        given(venueRepository.findByName(anyString())).willReturn(venue1.toMono())

        venueService.findByName("Venue1").block().also {
            assertEquals(venue1, it)
        }
    }
    @Test
    fun `should throw error if venue not found`(){
        given(venueRepository.findByName(anyString())).willReturn(Mono.empty())

        assertThrows(ResponseStatusException::class.java) { venueService.findByName("Venue").block() }
    }
    @Test
    fun `should list all venues`(){
        given(venueRepository.findAll()).willReturn(Flux.just(venue1,venue2))

        StepVerifier.create(venueService.findAll())
                .expectNext(venue1, venue2)
                .verifyComplete()
    }
    @Test
    fun `should patch venue by Id`(){
        val venueDto = VenueDto(name = "newName", avatar = null)
        given(venueRepository.findById(anyString())).willReturn(venue1.toMono())
        given(venueRepository.save(any(Venue::class.java)?:venue1)).willReturn(venue2.toMono())

        venueService.patchVenue(venueId = "idV", venueDto = venueDto).block().also {
            assertNotNull(it)
        }
    }
    @Test
    fun `should delete venue`(){
        given(venueRepository.deleteById(anyString())).willReturn(Mono.empty())

        venueService.deleteById(venueId = "idV").block()
    }
    @Test
    fun `should return error when venue not found in patch`(){
        val venueDto = VenueDto(name = "newName", avatar = null)
        given(venueRepository.findById(anyString())).willReturn(Mono.empty())

        assertThrows(ResponseStatusException::class.java){venueService.patchVenue(venueId = "idV", venueDto = venueDto).block()}
    }
    @Test
    fun `should find venue by id`(){
        given(venueRepository.findById(anyString())).willReturn(venue1.toMono())

        StepVerifier.create(venueService.findById(venueId = "ven")).expectNext(venue1).verifyComplete()
    }
    @Test
    fun `should handle not found`(){
        given(venueRepository.findById(anyString())).willReturn(Mono.empty())

        StepVerifier.create(venueService.findById("id")).expectNext(venue1).verifyComplete()
    }
}