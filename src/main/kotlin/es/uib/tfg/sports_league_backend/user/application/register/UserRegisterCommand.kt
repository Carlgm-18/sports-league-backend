package es.uib.tfg.sports_league_backend.user.application.register

import es.uib.tfg.sportsapi.dto.LicenseElement
import es.uib.tfg.sportsapi.dto.SignImageUrl
import es.uib.tfg.sportsapi.dto.UserCategory
import java.time.LocalDateTime

data class UserRegisterCommand (
    var id: Long? = null,
    var firstName: String,
    var lastName: String,
    var email: String,
    var password: String,
    var confirmPassword: String,
    var category: UserCategory,
    var createdAt: LocalDateTime,
    var profileImageUrl: String? = null,
    var licenses: List<LicenseElement>? = null,
    var signature: SignImageUrl? = null,
)