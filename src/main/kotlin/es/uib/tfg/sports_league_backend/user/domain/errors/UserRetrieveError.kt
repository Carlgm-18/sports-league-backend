package es.uib.tfg.sports_league_backend.user.domain.errors

import es.uib.tfg.sports_league_backend.user.infrastructure.controller.UserController.UserId

sealed interface UserRetrieveError {
    data class UserNotFound(val userId: UserId): UserRetrieveError
}