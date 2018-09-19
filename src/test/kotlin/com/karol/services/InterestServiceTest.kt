package com.karol.services

import com.karol.domain.Interest
import com.karol.repositories.InterestRespository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

class InterestServiceImplTest{
    @Mock
    lateinit var interestRepository: InterestRespository
    @InjectMocks
    lateinit var interestService: InterestServiceImpl
    val interest = Interest(concertId = "idC", username = "username", id = "some")
    @BeforeEach
    fun beforeEach() = MockitoAnnotations.initMocks(this)

    @Test
    fun `should delete by concertId and username`(){
        given(interestRepository.findAllByUsernameAndConcertId(anyString(), anyString())).willReturn(interest.toMono())
        given(interestRepository.deleteById(anyString())).willReturn(Mono.empty())

        interestService.deleteByUsernameAndConcertId(concertId = "idC", username = "username").block()
    }
}