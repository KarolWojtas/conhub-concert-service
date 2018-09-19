package com.karol

import com.karol.handlers.ConcertCommentHandler
import com.karol.handlers.ConcertHandler
import com.karol.handlers.InterestHandler
import com.karol.handlers.VenueHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Configuration
class ConcertVenueRouterConfig{
    @Bean
    fun venueRoutes(venueHandler: VenueHandler): RouterFunction<ServerResponse> = router {
        "/venues".nest {
            GET(""){venueHandler.findAllResponse()}
            "/{venueId}".nest {
                GET("", venueHandler::findById)
                PATCH("", venueHandler::patchById)
                DELETE("", venueHandler::deleteById)
                "/avatar".nest {
                    GET("", venueHandler::getVenueAvatarResponse)
                    accept(MediaType.MULTIPART_FORM_DATA).and(POST("")).invoke { venueHandler.saveVenueAvatarResponse(it) }
                }
            }
        }
    }
    @Bean
    fun concertRoutes(concertHandler: ConcertHandler, concertCommentHandler: ConcertCommentHandler): RouterFunction<ServerResponse> = router {
        "/concerts".nest {
            GET(""){ findConcertsBySearchParams(it, concertHandler)}
            DELETE("/comments/{commentId}/{username}", concertCommentHandler::deleteCommentById)
            GET("/query/{query}"){findConcertsByNameLike(it, concertHandler)}
            "/{concertId}".nest {
                GET("", concertHandler::findByIdResponse)
                PATCH("", concertHandler::patchByIdResponse)
                DELETE("", concertHandler::deleteByIdResponse)
                "/comments".nest {
                    GET("", concertCommentHandler::findAllByConcertIdResponse)
                    POST("/{username}", concertCommentHandler::saveCommentDtoResponse)
                }
            }
            }
        }
    @Bean
    fun interestRoutes(interestHandler: InterestHandler): RouterFunction<ServerResponse> = router {
        "/interests/{username}".nest {
            GET("", interestHandler::findConcertsByUsernameIntrestsResponse)
            POST("", interestHandler::postInterestResponse)
            DELETE("", interestHandler::deleteInterestResponse)
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



