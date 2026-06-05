package es.uib.tfg.sports_league_backend.user.application.update

data class UserUpdateCommand (
    var firstName: String?,
    var lastName: String?,
    var email: String?,
    var licenses: List<LicenseElement>? = null,
    var signature: SignImageUrl? = null,
)