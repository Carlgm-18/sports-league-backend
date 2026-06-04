package es.uib.tfg.sports_league_backend.request.domain.errors

sealed interface ResolveRequestError
sealed interface CreateRequestError
sealed interface RetrieveRequestError

object RequestNotFound : ResolveRequestError
object LeagueNotFound : ResolveRequestError
object ParticipantNotFound : ResolveRequestError, CreateRequestError, RetrieveRequestError
object TeamNotFound : ResolveRequestError, CreateRequestError, RetrieveRequestError
object UnauthorizedAction : ResolveRequestError, RetrieveRequestError
object InvalidRequestState : ResolveRequestError
object CouldNotCreateTeam : ResolveRequestError
object ParticipantAndTeamLeagueMissmatch: CreateRequestError
