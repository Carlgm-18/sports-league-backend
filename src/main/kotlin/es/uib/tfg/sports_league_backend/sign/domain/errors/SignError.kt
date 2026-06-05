package es.uib.tfg.sports_league_backend.sign.domain.errors

sealed interface SignRetrieveError

object SignNotFound : SignRetrieveError