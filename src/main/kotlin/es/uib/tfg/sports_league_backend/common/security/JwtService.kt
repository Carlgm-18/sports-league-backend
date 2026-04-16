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
    @Value($$"${jwt.secret}") private val secretKey: String,
    @Value($$"${jwt.expiration}") private val jwtExpiration: Long
) {
    private fun getSignInKey(): SecretKey {
        return Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun generateToken(email: String, userId: Int): String {
        return Jwts.builder()
            .claims(mapOf("userId" to userId)) // Metemos el ID del usuario como dato extra
            .subject(email)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSignInKey())
            .compact()
    }

    fun extractEmail(token: String): String {
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