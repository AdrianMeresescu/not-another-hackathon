package not.another.hackathon.backend.intializer

import mu.KotlinLogging
import not.another.hackathon.backend.properties.HackathonProperties
import not.another.hackathon.backend.user.SiteUserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class AdminAccountLoader(val siteUserRepository: SiteUserRepository, val bCryptPasswordEncoder: BCryptPasswordEncoder, val hackathonProperties: HackathonProperties) : CommandLineRunner {

    private val logger = KotlinLogging.logger {}

    override fun run(vararg args: String?) {

        Flux.fromIterable(hackathonProperties.adminAccounts).flatMap {
            logger.info("Creating user: $it")
            siteUserRepository.save(it.copy(password = bCryptPasswordEncoder.encode(it.password), roles = mutableSetOf("ADMIN", "USER")))
        }.thenMany(siteUserRepository.findAll()).subscribe({ logger.info("Created user $it") })
    }


}
