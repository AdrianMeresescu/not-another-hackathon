package reactive.test.reactivetest

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@RestController
@SpringBootApplication
class ReactiveTestApplication{

    @PostMapping("/post", consumes = arrayOf("application/json"))
    fun post(@RequestBody string: String): Mono<ServerResponse>? {
        return ServerResponse.status(HttpStatus.BAD_REQUEST).build()
    }


    @GetMapping("/get")
    fun get(): Mono<ServerResponse> {
        return ServerResponse.status(HttpStatus.BAD_REQUEST).build()
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(ReactiveTestApplication::class.java, *args)
}
