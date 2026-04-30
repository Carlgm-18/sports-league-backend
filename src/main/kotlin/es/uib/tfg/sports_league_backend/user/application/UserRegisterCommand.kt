package es.uib.tfg.sports_league_backend.user.application

import es.uib.tfg.sportsapi.dto.LicenceElement
import es.uib.tfg.sportsapi.dto.SignImageUrl
import es.uib.tfg.sportsapi.dto.UserCategory
import java.time.LocalDateTime

data class UserRegisterCommand (
    var id: Int = 0,
    var firstName: String,
    var lastName: String,
    var email: String,
    var password: String,
    var confirmPassword: String,
    var category: UserCategory,
    var createdAt: LocalDateTime,
    var profileImageUrl: String? = null,
    var licenses: List<LicenceElement>? = null,
    var signature: SignImageUrl? = null,
)