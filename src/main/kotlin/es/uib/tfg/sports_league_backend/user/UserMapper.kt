package es.uib.tfg.sports_league_backend.user

import es.uib.tfg.sports_league_backend.user.entities.User
import es.uib.tfg.sportsapi.dto.UserDetails

// 1. De Entidad (BD) a DTO (Frontend)
fun User.toDetailsDTO(): UserDetails {
    return UserDetails(
        id = this.id,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName
    )
}

// 2. Si un DTO tiene campos distintos o anidados, es súper explícito
fun User.toCustomProfileDTO(): CustomProfile {
    return CustomProfile(
        fullName = "${this.firstName} ${this.lastName}", // Lógica de presentación aquí, no en el controller
        email = this.email
    )
}