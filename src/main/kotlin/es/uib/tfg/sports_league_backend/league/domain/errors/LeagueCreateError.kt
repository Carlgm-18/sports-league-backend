package es.uib.tfg.sports_league_backend.league.domain.errors

sealed interface LeagueCreateError {
    object SportNotFound : LeagueCreateError
    object ConfigurationNotFound : LeagueCreateError
    object PunctuationSystemNotFound : LeagueCreateError
    object UserNotFound : LeagueCreateError
}