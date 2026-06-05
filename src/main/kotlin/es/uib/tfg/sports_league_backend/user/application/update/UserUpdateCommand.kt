package es.uib.tfg.sports_league_backend.user.application.update

import es.uib.tfg.sportsapi.dto.LicenseElement
import es.uib.tfg.sportsapi.dto.SignImageUrl

data class UserUpdateCommand (
    var firstName: String?,
    var lastName: String?,
    var licenses: List<LicenseElement>? = null
)