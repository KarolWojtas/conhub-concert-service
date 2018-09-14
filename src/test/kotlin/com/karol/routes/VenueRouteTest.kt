package com.karol.routes

import com.karol.RouterConfig
import com.karol.handlers.VenueHandler
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerResponse

@WebFluxTest
@RunWith(SpringRunner::class)
@Import(RouterConfig::class)
class VenueRouteTest{
    @Autowired lateinit var client: WebTestClient
    @MockBean lateinit var venueHandler: VenueHandler
    @Before
    fun setUp() {

    }
    @Test
    fun `webClient loads`() = Assert.assertNotNull(client)

}