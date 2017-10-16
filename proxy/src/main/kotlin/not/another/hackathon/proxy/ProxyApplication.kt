package not.another.hackathon.proxy

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.gateway.handler.predicate.RoutePredicates
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.Routes
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.*

@SpringBootApplication
class ProxyApplication {

    @Value("\${proxy.webapp-address}")
    val webappAddress: String? = null

    @Value("\${proxy.backend-address}")
    val backendAddress: String? = null

    @Bean
    fun csrfFilter() = CsrfFilter()

    @Bean
    fun routes(csrfFilter: CsrfFilter): RouteLocator {
        return Routes.locator()
                .route("backend")
                .uri(backendAddress)
                .predicate(RoutePredicates.path("/api/**"))
                .add(csrfFilter)
                .and()
                .route("webapp")
                .uri(webappAddress)
                .predicate(RoutePredicates.path("/**"))
                .add(csrfFilter)
                .and()
                .build()
    }

}

class CsrfFilter(val basePath: String = "/api") : WebFilter {

    var csrfMethods = listOf(HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PUT)

    override fun filter(exchange: ServerWebExchange?, chain: WebFilterChain?): Mono<Void> {
        return exchange!!.session.flatMap { checkToken(exchange, chain) }
    }

    fun checkToken(exchange: ServerWebExchange, chain: WebFilterChain?): Mono<Void> {
        if (exchange.request.method === HttpMethod.GET) {

            val csrfCookieValue = exchange.request.cookies.getFirst("CSRF_TOKEN")?.value?.toString()
            if (csrfCookieValue == null) {
                exchange.response.addCookie(ResponseCookie.from("CSRF_TOKEN", UUID.randomUUID().toString()).maxAge(-1).build())
            }

        } else if (exchange.request.uri.path.startsWith(basePath) && exchange.request.method in csrfMethods) {
            val csrfCookieValue = exchange.request.cookies.getFirst("CSRF_TOKEN")?.value?.toString()
            if (!exchange.request.headers["X_CSRF_TOKEN"]?.first().equals(csrfCookieValue)) {
                return Mono.fromRunnable {
                    exchange.response.statusCode = HttpStatus.FORBIDDEN
                }
            }
        }
        return chain!!.filter(exchange)
    }

}


//@Configuration
//@EnableWebSecurity
//class WebSecurityConfig : WebSecurityConfigurerAdapter() {
//
//    override fun configure(http: HttpSecurity) {
//        http.httpBasic().disable()
//        http.formLogin().disable()
//        http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
//    }
//}


fun main(args: Array<String>) {
    SpringApplication.run(ProxyApplication::class.java, *args)
}
