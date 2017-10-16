package not.another.hackathon.backend.exception

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono


class UserAlreadyExistsException(message: String = "") : RuntimeException(message)
class AlreadyLoggedInException(message: String = "") : RuntimeException(message)
class UserNotFoundException(message: String = "") : RuntimeException(message)

data class ErrorMessage(var message: String? = "")

@Component
class ExceptionHandler(var objectMapper: ObjectMapper) : WebExceptionHandler {

    private val logger = KotlinLogging.logger {}

    override fun handle(exchange: ServerWebExchange?, ex: Throwable?): Mono<Void> {
        /* Handle different exceptions here */
        when (ex!!) {
            is UserAlreadyExistsException -> exchange!!.response.statusCode = HttpStatus.CONFLICT
            is AlreadyLoggedInException -> exchange!!.response.statusCode = HttpStatus.BAD_REQUEST
            is UserNotFoundException -> exchange!!.response.statusCode = HttpStatus.NOT_FOUND
        }

        logger.warn { "Exception occurred: ${ex.message}" }

        exchange!!.response.headers["content-type"] = "application/json"
        return exchange!!.response.writeWith(toDataBuffer(ex))
    }

    private fun toDataBuffer(t: Throwable): Mono<DataBuffer> {
        return Mono.just(DefaultDataBufferFactory().wrap(this.objectMapper.writeValueAsBytes(ErrorMessage(t.message))))
    }
}