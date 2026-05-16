package es.uib.tfg.sports_league_backend.league.domain.errors

sealed interface LeagueRetrieveError {
    object LeagueNotFound : LeagueRetrieveError
}