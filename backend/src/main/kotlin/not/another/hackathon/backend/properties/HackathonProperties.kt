package not.another.hackathon.backend.properties

import not.another.hackathon.backend.user.SiteUser
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("hackathon")
data class HackathonProperties(var adminAccounts: List<SiteUser> = listOf())