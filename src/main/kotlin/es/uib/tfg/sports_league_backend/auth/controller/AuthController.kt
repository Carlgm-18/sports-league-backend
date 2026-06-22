package es.uib.tfg.sports_league_backend.auth.controller

import es.uib.tfg.sports_league_backend.common.security.JwtService
import es.uib.tfg.sports_league_backend.common.security.TokenType
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.application.UserService
import es.uib.tfg.sports_league_backend.user.application.login.LoginSessionInfo
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toLoginResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class RefreshTokenRequest(
    val refreshToken: String
)

@RestController
class AuthController(
    private val jwtService: JwtService,
    private val userService: UserService,
    @Value($$"${JWT_EXPIRATION}") private val jwtExpirationMs: Long
) {

    @PostMapping("/api/v1/auth/token/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<*> {
        val refreshToken = request.refreshToken
        if (!jwtService.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                mapOf("error" to "Invalid refresh token")
            )
        }

        val userId = jwtService.extractUserId(refreshToken)
        return when (val userResult = userService.findUserById(userId)) {
            is DomainResult.Success -> {
                val user = userResult.data
                val newAccessToken = jwtService.generateToken(user.id!!, TokenType.ACCESS)
                val newRefreshToken = jwtService.generateToken(user.id!!, TokenType.REFRESH)
                val sessionInfo = LoginSessionInfo(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken,
                    expiresIn = jwtExpirationMs,
                    tokenType = "Bearer",
                    user = user
                )
                ResponseEntity.ok(sessionInfo.toLoginResponse())
            }
            is DomainResult.Failure -> {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf("error" to "User not found")
                )
            }
        }
    }
}