package es.uib.tfg.sports_league_backend.match.domain.errors

sealed interface MatchRetrieveError
sealed interface MatchUpdateError
sealed interface ProposalCreateError
sealed interface ProposalResolveError
sealed interface ProposalRetrieveError
sealed interface AssignRefereeError

object MatchNotFound : MatchRetrieveError, ProposalCreateError, AssignRefereeError, MatchUpdateError
object InexistentAvailabilityForThisRound : ProposalCreateError
object ProposalNotFound : ProposalRetrieveError, ProposalResolveError
object ParticipantNotFound : ProposalRetrieveError, ProposalCreateError, ProposalResolveError, AssignRefereeError
object LeagueNotFound : ProposalRetrieveError, ProposalCreateError, ProposalResolveError
object UserNotFound : ProposalRetrieveError, ProposalCreateError, ProposalResolveError
object ResolveOwnProposalError : ProposalResolveError
object ProposalAlreadyResolved : ProposalResolveError
object NoAvailableReferees : AssignRefereeError
object MatchNotScheduledYet : AssignRefereeError
object IncompatibleMatchAndReferee : AssignRefereeError
object NotRefereeParticipant : AssignRefereeError
object ScheduleAlreadyTaken : ProposalCreateError
object RefereeNotAssigned : MatchUpdateError
object MatchAlreadyEnded : MatchUpdateError
object TeamNotInMatch : MatchUpdateError
object TeamNotFound : MatchUpdateError
object InvalidState : MatchUpdateError

