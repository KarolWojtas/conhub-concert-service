package com.karol

import com.karol.handlers.ConcertHandler
import com.karol.handlers.VenueHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

@Configuration
class RouterConfig{
    @Bean
    fun venueRoutes(venueHandler: VenueHandler): RouterFunction<ServerResponse> = router {
        "/venues".nest {
            "/{venueName}".nest {
                GET("/concerts"){venueHandler.findConcertsByVenueName(it.pathVariable("venueName"))}
            }
        }
    }
    @Bean
    fun concertRoutes(concertHandler: ConcertHandler): RouterFunction<ServerResponse> = router {
        "/concerts".nest {
            GET("/query/{query}"){findConcertsByNameLike(it, concertHandler)}
            }
        }

    fun findConcertsByNameLike(request: ServerRequest, concertHandler: ConcertHandler):Mono<ServerResponse> =
            with(request){
                val by: String? = queryParam("by").orElse(null)
                val direction: String? = queryParam("direction").orElse(null)
                val page: String? = queryParam("page").orElse(null)
                val size: String? = queryParam("size").orElse(null)
                concertHandler.findConcertsByNameResponse(name = pathVariable("query"),
                        by = by, direction = direction, size = size?.toInt(), page = page?.toInt())

            }
    }



