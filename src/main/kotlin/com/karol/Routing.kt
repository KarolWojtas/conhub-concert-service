package com.karol

import com.karol.handlers.ConcertHandler
import com.karol.handlers.VenueHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

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
            GET(""){ findConcertsBySearchParams(it, concertHandler)}
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
    fun findConcertsBySearchParams(request: ServerRequest, concertHandler: ConcertHandler): Mono<ServerResponse> =
        with(request) {
            val name = queryParam("name").orElse(null)
            val venues: List<String> = this.queryParams().filterKeys { it == "venue" }.flatMap { it.value }
            val by: String? = queryParam("by").orElse(null)
            val direction: String? = queryParam("direction").orElse(null)
            val page: String? = queryParam("page").orElse(null)
            val size: String? = queryParam("size").orElse(null)
            val before: LocalDateTime? = if (queryParam("before").isPresent) LocalDateTime.parse(queryParam("before").get()) else null
            val after: LocalDateTime? = if (queryParam("after").isPresent) LocalDateTime.parse(queryParam("after").get()) else null
            concertHandler.findConcertsBySearchParams(name = name, by = by, direction = direction, size = size?.toLong(), page = page?.toLong(),
                    before = before, after = after, venues = if(venues.isEmpty()) null else venues)


    }


