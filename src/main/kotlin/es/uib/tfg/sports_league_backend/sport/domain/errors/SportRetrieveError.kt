package es.uib.tfg.sports_league_backend.sport.domain.errors

sealed interface SportRetrieveError {
    object SportNotFound: SportRetrieveError
}