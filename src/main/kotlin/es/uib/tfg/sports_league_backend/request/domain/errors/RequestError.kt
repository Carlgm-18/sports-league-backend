package es.uib.tfg.sports_league_backend.request.domain.errors

sealed interface RequestError

object RequestNotFound : RequestError
object LeagueNotFound : RequestError
object ParticipantNotFound : RequestError
object TeamNotFound : RequestError
object UnauthorizedAction : RequestError
object InvalidRequestState : RequestError
