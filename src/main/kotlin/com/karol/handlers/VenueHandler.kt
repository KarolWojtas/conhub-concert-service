package com.karol.handlers

import com.karol.domain.Venue
import com.karol.domain.VenueDto
import com.karol.services.ConcertService
import com.karol.services.VenueService
import org.apache.commons.io.IOUtils
import org.bouncycastle.util.io.Streams
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam


@Component
class VenueHandler {
    @Autowired lateinit var venueService: VenueService
    @Autowired lateinit var concertService: ConcertService

    fun findConcertsByVenueName(venueName: String) = ServerResponse.ok().body(concertsByVenueNameFlux(venueName))

    fun concertsByVenueNameFlux(venueName: String) = venueService.findByName(venueName)
            .flatMapMany { concertService.findAllByVenueId(it?.id?: "")}

    fun findAllResponse() = ServerResponse.ok().body(venueService.findAll())

    fun findById(request: ServerRequest) = ServerResponse.ok()
            .body(venueService.findById(request.pathVariable("venueId"))
                    .filter { it.id != null }
                    .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND))))

    fun patchVenueMono(venueId: String, venueDto: Mono<VenueDto> ) =
            venueDto.flatMap { venueService.patchVenue(venueId = venueId, venueDto = it) }
    fun patchById(req: ServerRequest) = ServerResponse.accepted()
            .body(patchVenueMono(venueId = req.pathVariable("venueId"), venueDto = req.bodyToMono(VenueDto::class.java)))
    fun deleteById(req: ServerRequest) = ServerResponse.accepted().body(venueService.deleteById(req.pathVariable("venueId")))

    fun bufferedImageFlux(venueId: String): Flux<DataBuffer> = venueService
            .findById(venueId)
            .switchIfEmpty(Mono.empty())
            .map { it.avatar }
            .map { DefaultDataBufferFactory().wrap(it?: ByteArray(1)) }
            .flatMapMany { Flux.just(it) }
    fun getVenueAvatarResponse(req: ServerRequest) = ServerResponse.ok().body(bufferedImageFlux(req.pathVariable("venueId")))

    fun postVenueAvatarMono(venueId: String, req: ServerRequest): Flux<Venue> =
            req.body(BodyExtractors.toMultipartData())
            .map { it.toSingleValueMap() }
            .map { it["image"]  }
                    .flatMapMany { it?.content() }
                    .buffer(Integer.MAX_VALUE)
                    .flatMap { DataBufferUtils.join(it.toFlux()) }
                    .map { it.asInputStream().compressImage(0.5F)}
                    .onErrorMap { ResponseStatusException(HttpStatus.BAD_REQUEST) }
            .zipWith( venueService.findById(venueId))
                    .map { Venue(id = it.t2.id?:"", name = it.t2.name, avatar = it.t1) }
                    .flatMap { venueService.save(it) }
                    .map { it }
    fun saveVenueAvatarResponse(req: ServerRequest) = ServerResponse.accepted()
            .body(postVenueAvatarMono(req.pathVariable("venueId"), req))


}
fun InputStream.compressImage(ratio: Float): ByteArray{
    var imageCompressed: ByteArray = "empty".toByteArray()
    ByteArrayOutputStream().use {baos ->
        ImageIO.createImageOutputStream(baos).use { ios ->
            val image = ImageIO.read(this)
            val imageWriter = ImageIO.getImageWritersByFormatName("jpg").next()
            imageWriter.output = ios
            val param = imageWriter.defaultWriteParam
            param.compressionMode = ImageWriteParam.MODE_EXPLICIT
            param.compressionQuality = ratio
            imageWriter.write(null, IIOImage(image, null,null), param )
            imageCompressed = baos.toByteArray()
        }
    }
    return imageCompressed


}