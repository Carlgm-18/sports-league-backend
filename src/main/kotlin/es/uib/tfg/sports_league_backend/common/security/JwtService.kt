package es.uib.tfg.sports_league_backend.common.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value($$"${JWT_SECRET}") private val secretKey: String,
    @Value($$"${JWT_EXPIRATION}") private val jwtExpiration: Long,
    @Value($$"${JWT_REFRESH_EXPIRATION}") private val jwtRefreshExpiration: Long
) {
    private fun getSignInKey(): SecretKey {
        return Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun generateToken(userId: Int, type: TokenType): String {
        val tokenExpiration = when (type) {
            TokenType.ACCESS -> jwtExpiration
            TokenType.REFRESH -> jwtRefreshExpiration
        }

        val claims = when (type) {
            TokenType.ACCESS -> mapOf("userId" to userId)
            TokenType.REFRESH -> mapOf("userId" to userId)
        }

        return Jwts.builder()
            .claims(claims)
            .subject(userId.toString())
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + tokenExpiration))
            .signWith(getSignInKey())
            .compact()

    }

    fun extractUserId(token: String): String {
        return extractAllClaims(token).subject
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            extractAllClaims(token).expiration.after(Date())
        } catch (e: Exception) {
            false
        }
    }
}