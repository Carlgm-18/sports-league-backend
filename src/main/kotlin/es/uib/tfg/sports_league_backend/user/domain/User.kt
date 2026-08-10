package es.uib.tfg.sports_league_backend.user.domain

import es.uib.tfg.sportsapi.dto.SignImageUrl
import es.uib.tfg.sportsapi.dto.UserCategory
import java.time.LocalDateTime

class User(
    var id: Long? = null,
    var firstName: String,
    var lastName: String,
    var email: String,
    var passwordHash: SecurePassword,
    var category: UserCategory,
    var createdAt: LocalDateTime,
    var profileImageUrl: String? = null,
    var licenses: MutableSet<RefereeLicense> = mutableSetOf(),
    var signature: SignImageUrl? = null,
)