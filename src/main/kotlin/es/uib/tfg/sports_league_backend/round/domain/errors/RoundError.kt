package es.uib.tfg.sports_league_backend.round.domain.errors

sealed interface RoundRetrieveError

object RoundNotFound: RoundRetrieveError