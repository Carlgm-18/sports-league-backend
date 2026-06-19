package es.uib.tfg.sports_league_backend.incidence.domain.errors

sealed interface IncidenceCreateError
sealed interface IncidenceRetrieveError

object ParticipantNotFound : IncidenceCreateError
object IncidenceNotFound : IncidenceRetrieveError