package es.uib.tfg.sports_league_backend.team.domain.error

sealed interface TeamRetrieveError

object TeamNotFound : TeamRetrieveError