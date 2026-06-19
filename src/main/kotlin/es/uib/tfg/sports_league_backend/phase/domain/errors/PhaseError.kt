package es.uib.tfg.sports_league_backend.phase.domain.errors

sealed interface PhaseRetrieveError
sealed interface HandlePhaseError

object PhaseNotFound : PhaseRetrieveError
object LastPhaseError : HandlePhaseError