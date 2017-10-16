package not.another.hackathon.backend

import com.fasterxml.jackson.databind.ObjectMapper
import not.another.hackathon.backend.properties.HackathonProperties
import not.another.hackathon.backend.user.LoginRequest
import not.another.hackathon.backend.user.SiteUserDetailsRepository
import org.apache.commons.io.IOUtils
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
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.logout.LogoutHandler
import org.springframework.security.web.server.context.WebSessionSecurityContextRepository
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.web.server.ServerWebExchange
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
        http.authorizeExchange().pathMatchers("/api/exists").permitAll()
        http.authorizeExchange().pathMatchers("/api/login").permitAll()
        http.authorizeExchange().pathMatchers("/api/admin/**").hasAuthority("ADMIN")
        http.authorizeExchange().pathMatchers("/api/user/**").authenticated()
        http.authorizeExchange().pathMatchers("/api/logout").authenticated()

        http.logout().logoutUrl("/api/logout").logoutHandler(SimpleLogoutHandler())

        val authenticationFilter = AuthenticationWebFilter(manager)
        authenticationFilter.setRequiresAuthenticationMatcher(AndServerWebExchangeMatcher(listOf(
                PathPatternParserServerWebExchangeMatcher("/api/login", HttpMethod.POST),
                ServerWebExchangeMatcher {
                    exchange ->
                    if (SecurityContextHolder.getContext()?.authentication?.name != null) {
                        return@ServerWebExchangeMatcher ServerWebExchangeMatcher.MatchResult.notMatch()
                    } else {
                        return@ServerWebExchangeMatcher ServerWebExchangeMatcher.MatchResult.match()
                    }
                })
        ))
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


class SimpleLogoutHandler : LogoutHandler {

    val repository = WebSessionSecurityContextRepository()

    override fun logout(exchange: WebFilterExchange, authentication: Authentication?): Mono<Void> {
        return repository.save(exchange.exchange, null as SecurityContext?).then(returnOk(exchange.exchange))
    }

    fun returnOk(exchange: ServerWebExchange): Mono<Void> {
        return Mono.fromRunnable { exchange.response.statusCode = HttpStatus.OK }
    }

}


fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
