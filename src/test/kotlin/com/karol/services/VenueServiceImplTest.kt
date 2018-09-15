package com.karol.services

import com.karol.domain.Venue
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
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

class VenueServiceUnitTest{
    @Mock lateinit var venueRepository: VenueRepository
    @InjectMocks lateinit var venueService: VenueServiceImpl
    val venue1 = Venue(id="id1", name = "Venue1")
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
}