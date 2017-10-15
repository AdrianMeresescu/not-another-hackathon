package not.another.hackathon.proxy

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.gateway.handler.predicate.RoutePredicates
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.Routes
import org.springframework.context.annotation.Bean

@SpringBootApplication
class ProxyApplication {

    @Value("\${proxy.webapp-address}")
    val webappAddress: String? = null

    @Value("\${proxy.backend-address}")
    val backendAddress: String? = null

    @Bean
    fun routes(): RouteLocator {
        return Routes.locator()
                .route("backend")
                .uri(backendAddress)
                .predicate(RoutePredicates.path("/api/**"))
                .and()
                .route("webapp")
                .uri(webappAddress)
                .predicate(RoutePredicates.path("/**"))
                .and()
                .build()
    }

}


fun main(args: Array<String>) {
    SpringApplication.run(ProxyApplication::class.java, *args)
}
