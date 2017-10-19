package not.another.hackathon.backend.user

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Document
data class SiteUser(@Id var email: String = "",
                    var password: String = "",
                    var enabled: Boolean = true,
                    var data: Map<String, Any?> = mutableMapOf(),
                    var roles: MutableSet<String> = mutableSetOf()) {

    fun toSpringUser(): User {
        val authorities = mutableSetOf<SimpleGrantedAuthority>()
        roles.forEach { authorities.add(SimpleGrantedAuthority(it)) }
        return User(email, password, enabled, enabled, enabled, enabled, authorities)
    }
}

interface SiteUserRepository : ReactiveMongoRepository<SiteUser, String>

@Component
class SiteUserDetailsRepository(val siteUserRepository: SiteUserRepository) : UserDetailsRepository {
    override fun findByUsername(username: String?): Mono<UserDetails> {
        return siteUserRepository.findById(username).map(SiteUser::toSpringUser)
    }
}

