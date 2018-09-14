package com.karol

import com.karol.handlers.VenueHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicate
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class RouterConfig{
    @Bean
    fun venueRoutes(venueHandler: VenueHandler): RouterFunction<ServerResponse> = router {
        "/venues".nest {
            GET("/{venueName}/concerts")(venueHandler::findConcertsByVenueName)
        }
    }
}