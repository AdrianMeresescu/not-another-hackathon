package not.another.hackathon.backend

import com.fasterxml.jackson.databind.ObjectMapper
import not.another.hackathon.backend.properties.HackathonProperties
import not.another.hackathon.backend.user.LoginRequest
import not.another.hackathon.backend.user.SiteUserDetailsRepository
import not.another.hackathon.backend.user.SiteUserRepository
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.HttpSecurity.http
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.WebSessionSecurityContextRepository
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@EnableConfigurationProperties(HackathonProperties::class)
@SpringBootApplication
@EnableWebFluxSecurity
class Application {

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(siteUserDetailsRepository: SiteUserDetailsRepository, bCryptPasswordEncoder: BCryptPasswordEncoder): ReactiveAuthenticationManager {
        val authenticationManager = UserDetailsRepositoryAuthenticationManager(siteUserDetailsRepository)
        authenticationManager.setPasswordEncoder(bCryptPasswordEncoder)
        return authenticationManager
    }

    @Bean
    @Throws(Exception::class)
    fun springSecurityFilterChain(manager: ReactiveAuthenticationManager): SecurityWebFilterChain {
        val http = http()
        http.authenticationManager(manager)
        http.formLogin().disable()
        http.httpBasic().disable()
        http.authorizeExchange().pathMatchers("/api/register").permitAll()
        http.authorizeExchange().pathMatchers("/api/login").permitAll()
        http.authorizeExchange().pathMatchers("/api/admin/**").hasRole("ADMIN")
        http.authorizeExchange().pathMatchers("/api/user/**").authenticated()

        val authenticationFilter = AuthenticationWebFilter(manager)
        authenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/api/login"))
        authenticationFilter.setAuthenticationFailureHandler({
            webFilterExchange, _ ->
            Mono.fromRunnable { webFilterExchange.exchange.response.statusCode = HttpStatus.FORBIDDEN }
        })
        authenticationFilter.setAuthenticationConverter({
            serverWebExchange ->
            serverWebExchange.request.body.single().map { ObjectMapper().readValue(IOUtils.toString(it.asInputStream()), LoginRequest::class.java) }
                    .map { UsernamePasswordAuthenticationToken(it.username, it.password) }
        })
        authenticationFilter.setAuthenticationSuccessHandler({
            _, webFilterExchange ->
            Mono.fromRunnable { webFilterExchange.exchange.response.statusCode = HttpStatus.OK }
        })
        authenticationFilter.setSecurityContextRepository(WebSessionSecurityContextRepository())
        http.addFilterAt(authenticationFilter, SecurityWebFiltersOrder.FORM_LOGIN)
        return http.build()
    }

}

@Component
class AdminAccountLoader(val siteUserRepository: SiteUserRepository, val bCryptPasswordEncoder: BCryptPasswordEncoder, val hackathonProperties: HackathonProperties) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(Application::class.java)

    override fun run(vararg args: String?) {

        Flux.fromIterable(hackathonProperties.adminAccounts).flatMap {
            log.info("Creating user: $it")
            siteUserRepository.save(it.copy(password = bCryptPasswordEncoder.encode(it.password), roles = mutableSetOf("ADMIN", "USER")))
        }.thenMany(siteUserRepository.findAll()).subscribe({ log.info("Created user $it") })
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
