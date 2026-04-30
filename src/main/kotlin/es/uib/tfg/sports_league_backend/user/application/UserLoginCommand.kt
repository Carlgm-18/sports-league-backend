package es.uib.tfg.sports_league_backend.user.application

data class UserLoginCommand(
    val email: String,
    val password: String
)