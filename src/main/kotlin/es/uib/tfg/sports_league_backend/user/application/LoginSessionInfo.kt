package es.uib.tfg.sports_league_backend.user.application

import es.uib.tfg.sports_league_backend.user.domain.User

data class LoginSessionInfo(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String,
    val user: User,
)