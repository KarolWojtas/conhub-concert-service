package com.karol.handlers

import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.karol.domain.Concert
import com.karol.domain.Interest
import com.karol.domain.InterestDto
import com.karol.services.ConcertService
import com.karol.services.InterestService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class InterestHandler{
    @Autowired
    lateinit var interestService: InterestService
    @Autowired
    lateinit var concertService: ConcertService
    @Autowired
    lateinit var webClientService: WebClientService

    fun concertsByUsernameFlux(username: String): Flux<Concert> = interestService.findAllByUsername(username)
            .filterWhen{webClientService.checkUsernameAsync(username)}
            .flatMap{ concertService.findById(it.concertId)}


    fun findConcertsByUsernameIntrestsResponse(req: ServerRequest): Mono<ServerResponse> {
        val username = req.pathVariable("username")
        val fileResponse = req.queryParam("file").orElseGet { "none" }
        println(fileResponse)
        return when(fileResponse){
            "pdf" -> ServerResponse.ok().header("Content-Disposition", "inline").contentType(MediaType.APPLICATION_PDF).body(concertsByUsernamePdfFlux(username))
            else -> ServerResponse.ok().body(concertsByUsernameFlux(username))
        }
    }

    fun postInterestMono(interestDto: Mono<InterestDto>, username: String): Mono<Interest> = interestDto.flatMap { concertService.findById(it.concertId) }
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND,"Concert not found")))
            .filterWhen { webClientService.checkUsernameAsync(username) }
            .map { Interest(id = null, username = username, concertId = it.id?:"null") }
            .filter { it.concertId != "null" }
            .flatMap { interestService.postInterest(it) }
    fun postInterestResponse(req: ServerRequest): Mono<ServerResponse> = with(req){
        ServerResponse.accepted().body(postInterestMono(interestDto = this.bodyToMono(InterestDto::class.java), username = this.pathVariable("username")))
    }
    fun deleteInterestResponse(req: ServerRequest): Mono<ServerResponse> = ServerResponse.accepted().body(
            req.bodyToMono(InterestDto::class.java).filterWhen { webClientService.checkUsernameAsync(req.pathVariable("username")) }
                    .flatMap { concertService.findById(it.concertId) }
                    .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND,"Concert not found")))
                    .flatMap { interestService.deleteByUsernameAndConcertId(concertId = it.id?:"null", username = req.pathVariable("username")) }

    )
    fun concertsByUsernamePdfFlux(username: String): Flux<DataBuffer> = mapConcertsToPdfByteArray(concertsByUsernameFlux(username), username)
            .map { DefaultDataBufferFactory().wrap(it?: ByteArray(1))  }
            .flatMapMany { Flux.just(it) }

    fun mapConcertsToPdfByteArray(concerts: Flux<Concert>, username: String): Mono<ByteArray> {
        val baos = ByteArrayOutputStream()
        val font = FontFactory.getFont(FontFactory.HELVETICA, "Cp1250", BaseFont.EMBEDDED)
        val document = Document().apply {
            addHeader("Main header", "Concerts for $username")
            PdfWriter.getInstance(this, baos)
            open()
            add(Paragraph("Concerts liked by $username"))
        }

        val table = PdfPTable(floatArrayOf(4f,4f,4f))

        return Flux.just("Concert", "Venue", "Date")
                .map { PdfPCell().apply { backgroundColor = BaseColor.LIGHT_GRAY; borderWidth = 2f; phrase = Phrase(it, font) } }
                .map { table.addCell(it) }
                .switchMap { concerts }
                .map { table.addCell(Phrase(it.name, font)); table.addCell(Phrase(it.venue.name, font));
                    table.addCell(Phrase(it.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), font));
                    it }
                .reduce(document){acc, item -> acc}
                .map { it.add(table); it.close(); baos.use { it.toByteArray() } }

    }
}