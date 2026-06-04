package es.uib.tfg.sports_league_backend.phase.domain.errors

sealed interface PhaseRetrieveError

object PhaseNotFound : PhaseRetrieveError