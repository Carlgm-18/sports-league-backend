package es.uib.tfg.sports_league_backend.user

import es.uib.tfg.sports_league_backend.user.entities.User
import es.uib.tfg.sportsapi.dto.UserCreateRequest
import es.uib.tfg.sportsapi.dto.UserDetails
import java.net.URI
import java.time.LocalDateTime

// 1. De Entidad (BD) a DTO (Frontend)
fun User.toDetailsDTO(): UserDetails {
    return UserDetails(
        userId = this.id,
        email = this.email,
        fullName = "${this.firstName} ${this.lastName}",
        category = this.category,
        profileImageUrl = URI(this.profileImageUrl ?: ""),
        licenses = this.licenses ?: listOf(),
        signature = this.signature,
        createdAt = this.createdAt
    )
}

fun UserCreateRequest.toEntity(encodedPassword: String): User {
    return User(
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        passwordHash = encodedPassword,
        category = this.category,
        createdAt = LocalDateTime.now(),
        licenses = this.licenses
    )
}

fun User.toCreateResponse(): UserCreateResponse {
    return UserCreateResponse(
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        licenses = this.licenses,
        category = this.category,
    )
}