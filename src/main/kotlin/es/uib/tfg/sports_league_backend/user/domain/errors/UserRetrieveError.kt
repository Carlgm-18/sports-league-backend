package es.uib.tfg.sports_league_backend.user.domain.errors

sealed interface UserRetrieveError {
    object UserNotFound: UserRetrieveError
}