package es.uib.tfg.sports_league_backend.user

import es.uib.tfg.sports_league_backend.user.entities.User
import es.uib.tfg.sportsapi.dto.UserDetails

// 1. De Entidad (BD) a DTO (Frontend)
fun User.toDetailsDTO(): UserDetails {
    return UserDetails(
        userId = this.id,
        email = this.email,
        fullName = this.firstName + this.lastName,
        category = TODO(),
        profileImageUrl = TODO(),
        licenses = TODO(),
        signature = TODO(),
    )
}