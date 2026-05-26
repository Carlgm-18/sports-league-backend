package es.uib.tfg.sports_league_backend.participant.domain.errors

sealed interface ParticipantJoinError
sealed interface ParticipantRetrieveError

object LeagueNotFound : ParticipantRetrieveError
object UserNotFound : ParticipantRetrieveError
object AlreadyParticipant : ParticipantJoinError