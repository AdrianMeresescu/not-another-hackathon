package not.another.hackathon.backend.user

import mu.KotlinLogging
import not.another.hackathon.backend.exception.AlreadyLoggedInException
import not.another.hackathon.backend.exception.UserAlreadyExistsException
import not.another.hackathon.backend.exception.UserNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.Principal


@RestController
@RequestMapping("/api")
class UserController(val siteUserRepository: SiteUserRepository, val bCryptPasswordEncoder: BCryptPasswordEncoder) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/login")
    fun login(): Mono<ServerResponse> {
        return ServerResponse.ok().build()
    }

    @PostMapping("/register", consumes = arrayOf("application/json"))
    fun register(@RequestBody siteUser: SiteUser, principal: Principal?): Mono<ServerResponse> {
        if (principal == null) {
            return siteUserRepository.existsById(siteUser.email)
                    .flatMap {
                        if (it)
                            throw UserAlreadyExistsException("User already registered: ${siteUser.email}")
                        else
                            siteUserRepository.save(siteUser.copy(password = bCryptPasswordEncoder.encode(siteUser.password), roles = mutableSetOf("USER")))
                    }.doOnNext { logger.info("Created User: $it") }
                    .then(ServerResponse.ok().build())
        } else {
            throw AlreadyLoggedInException("Already logged in as: ${principal.name}")
        }
    }

    @GetMapping("/user/self")
    fun self(principal: Principal): Mono<SiteUser> {
        return siteUserRepository.findById(principal.name).map { it.copy(password = "") }
    }


    @PostMapping("/user/self/update")
    fun updateSelf(@RequestBody siteUser: SiteUser, principal: Principal): Mono<ServerResponse> {
        return siteUserRepository.findById(principal.name).flatMap {
            if (it == null) {
                throw UserNotFoundException("User not found: ${principal.name}}")
            } else {
                it.data = siteUser.data
                siteUserRepository.save(it)
            }
        }.then(ServerResponse.ok().build())
    }

    @PostMapping("/user/self/change-password")
    fun changePassword(@RequestBody passwordChangeRequest: PasswordChangeRequest, principal: Principal): Mono<ServerResponse> {
        return siteUserRepository.findById(principal.name).flatMap {
            if (it == null) {
                throw UserNotFoundException("User not found: ${principal.name}}")
            } else {
                it.password = bCryptPasswordEncoder.encode(passwordChangeRequest.newPassword)
                siteUserRepository.save(it)
            }
        }.then(ServerResponse.ok().build())
    }

    @GetMapping("/admin/user/all")
    fun all(): Flux<SiteUser> {
        return siteUserRepository.findAll()
    }

    @DeleteMapping("/admin/user/{email}")
    fun delete(@PathVariable email: String): Mono<ServerResponse> {
        return siteUserRepository.deleteById(email).then(ServerResponse.ok().build())
                .switchIfEmpty(Mono.error(UserNotFoundException("User not found $email")))
    }

    @PostMapping("/admin/user/{email}")
    fun updateUser(@PathVariable email: String, @RequestBody siteUser: SiteUser): Mono<ServerResponse> {
        return siteUserRepository.findById(siteUser.email).map {
            it.data = siteUser.data
            it.enabled = siteUser.enabled
            siteUserRepository.save(it)
        }.then(ServerResponse.ok().build())
                .switchIfEmpty(Mono.error(UserNotFoundException("User not found $email")))
    }

    @PostMapping("/admin/user/change-password/{email}")
    fun changeUserPassword(@PathVariable email: String, @RequestBody passwordChangeRequest: PasswordChangeRequest): Mono<ServerResponse> {
        return siteUserRepository.findById(email).map {
            it.password = bCryptPasswordEncoder.encode(passwordChangeRequest.newPassword)
            siteUserRepository.save(it)
        }.then(ServerResponse.ok().build())
                .switchIfEmpty(Mono.error(UserNotFoundException("User not found $email")))
    }

}


data class PasswordChangeRequest(var newPassword: String = "")

data class LoginRequest(var username: String = "", var password: String = "")