package not.another.hackathon.backend.user

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.Principal

@Document
data class SiteUser(@Id var email: String = "",
                    @JsonIgnore var password: String = "",
                    var enabled: Boolean = true,
                    var data: Map<String, Any?> = mutableMapOf(),
                    var roles: MutableSet<String> = mutableSetOf()) {

    fun toSpringUser(): User {
        val authorities = mutableSetOf<SimpleGrantedAuthority>()
        roles.forEach { authorities.add(SimpleGrantedAuthority(it)) }
        return User(email, password, enabled, enabled, enabled, enabled, authorities)
    }
}

data class PasswordChangeRequest(var newPassword: String = "")

data class LoginRequest(var username: String = "", var password: String = "")

interface SiteUserRepository : ReactiveMongoRepository<SiteUser, String>


@Component
class SiteUserDetailsRepository(val siteUserRepository: SiteUserRepository) : UserDetailsRepository {
    override fun findByUsername(username: String?): Mono<UserDetails> {
        return siteUserRepository.findById(username).map(SiteUser::toSpringUser)
    }

}


@RestController
@RequestMapping("/api")
class UserController(val siteUserRepository: SiteUserRepository, val bCryptPasswordEncoder: BCryptPasswordEncoder) {

    val webClient = WebClient
            .builder()
            .baseUrl("http://localhost:8080/api/login")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build()!!

    @PostMapping("/ajax-login")
    fun doLogin(@RequestBody loginRequest: LoginRequest, serverWebExchange: ServerWebExchange): Mono<ClientResponse>? {
        val map : MultiValueMap<String,String> = LinkedMultiValueMap()
        map.put("username", listOf(loginRequest.username))
        map.put("password",listOf(loginRequest.password))
        val resp = webClient.post().body(BodyInserters.fromFormData(map)).exchange()
        return resp;
    }

    @GetMapping("/login")
    fun login(): Mono<ServerResponse> {
        return ServerResponse.ok().build()
    }

    @PostMapping("/register")
    fun register(@RequestBody siteUser: SiteUser, principal: Principal?): Mono<ServerResponse> {
        if (principal == null) {
            return siteUserRepository
                    .findById(siteUser.email)
                    .then(ServerResponse.status(HttpStatus.FORBIDDEN).build())
                    .switchIfEmpty(siteUserRepository.save(siteUser.copy(password = bCryptPasswordEncoder.encode(siteUser.password), roles = mutableSetOf("USER")))
                            .then(ServerResponse.ok().build()))
        } else {
            return ServerResponse.status(HttpStatus.NOT_ACCEPTABLE).build()
        }
    }

    @GetMapping("/user/self")
    fun self(principal: Principal): Mono<SiteUser> {
        return siteUserRepository.findById(principal.name)
    }


    @PostMapping("/user/self/update")
    fun updateSelf(@RequestBody siteUser: SiteUser, @AuthenticationPrincipal currentUser: User): Mono<ServerResponse> {
        return siteUserRepository.findById(currentUser.username).map {
            it.data = siteUser.data
            siteUserRepository.save(it)
        }.then(ServerResponse.ok().build())
                .switchIfEmpty(ServerResponse.notFound().build())
    }

    @PostMapping("/user/self/change-password")
    fun changePassword(@RequestBody passwordChangeRequest: PasswordChangeRequest, @AuthenticationPrincipal currentUser: User): Mono<ServerResponse> {
        return siteUserRepository.findById(currentUser.username).map {
            it.password = bCryptPasswordEncoder.encode(passwordChangeRequest.newPassword)
            siteUserRepository.save(it)
        }.then(ServerResponse.ok().build())
                .switchIfEmpty(ServerResponse.notFound().build())
    }

    @GetMapping("/admin/user/all")
    fun all(): Flux<SiteUser> {
        return siteUserRepository.findAll()
    }

    @DeleteMapping("/admin/user/{email}")
    fun delete(@PathVariable email: String): Mono<ServerResponse> {
        return siteUserRepository.deleteById(email).then(ServerResponse.ok().build())
                .switchIfEmpty(ServerResponse.notFound().build())
    }

    @PostMapping("/admin/user/{email}")
    fun updateUser(@RequestBody siteUser: SiteUser): Mono<ServerResponse> {
        return siteUserRepository.findById(siteUser.email).map {
            it.data = siteUser.data
            it.enabled = siteUser.enabled
            siteUserRepository.save(it)
        }.then(ServerResponse.ok().build())
                .switchIfEmpty(ServerResponse.notFound().build())
    }

    @PostMapping("/admin/user/change-password/{email}")
    fun changeUserPassword(@PathVariable email: String, @RequestBody passwordChangeRequest: PasswordChangeRequest): Mono<ServerResponse> {
        return siteUserRepository.findById(email).map {
            it.password = bCryptPasswordEncoder.encode(passwordChangeRequest.newPassword)
            siteUserRepository.save(it)
        }.then(ServerResponse.ok().build())
                .switchIfEmpty(ServerResponse.notFound().build())
    }

}