package es.uib.tfg.sports_league_backend.request.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.application.LeagueService
import es.uib.tfg.sports_league_backend.participant.application.ParticipantService
import es.uib.tfg.sports_league_backend.participant.application.ParticipationRoleService
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantRole
import es.uib.tfg.sports_league_backend.request.domain.*
import es.uib.tfg.sports_league_backend.request.domain.errors.*
import es.uib.tfg.sports_league_backend.request.infrastructure.repository.RequestRepository
import es.uib.tfg.sports_league_backend.request.infrastructure.repository.TeamJoinRequestRepository
import es.uib.tfg.sports_league_backend.team.application.TeamService
import es.uib.tfg.sportsapi.dto.RequestState
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RequestService(
    private val requestRepository: RequestRepository,
    private val teamJoinRequestRepository: TeamJoinRequestRepository,
    private val leagueService: LeagueService,
    private val participantService: ParticipantService,
    private val participationRoleService: ParticipationRoleService,
    private val teamService: TeamService
) {

    private fun Participant.isCaptain(): Boolean =
        roles.any { it.participationRole.roleName == "CAPTAIN" }

    @Transactional
    fun createRefereeRequest(
        request: es.uib.tfg.sportsapi.dto.RefereeRequest
    ): DomainResult<RefereeRequest, ResolveRequestError> {
        val league = when(val result = leagueService.findLeagueById(request.participantId)) {
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> result.data
        }

        val participant = when(val result = participantService.findParticipantById(request.participantId)) {
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> result.data
        }

        val refereeRequest = RefereeRequest(
            league = league,
            participant = participant,
            status = RequestState.PENDING
        )
        return DomainResult.Success(requestRepository.save(refereeRequest))
    }

    @Transactional
    fun resolveRefereeRequest(
        requestId: Long,
        status: RequestState,
        rejectionReason: String?
    ): DomainResult<RefereeRequest, ResolveRequestError> {

        val request = requestRepository.findByIdOrNull(requestId) as? RefereeRequest
                        ?: return DomainResult.Failure(RequestNotFound)

        if (request.status != RequestState.PENDING)
            return DomainResult.Failure(InvalidRequestState)

        request.status = status
        request.resolvedAt = LocalDateTime.now()

        if (status == RequestState.ACCEPTED) {
            val targetParticipant = request.participant

            val refereeRole = participationRoleService.findRoleByName("REFEREE")
            val newRole = ParticipantRole(participant = targetParticipant, participationRole = refereeRole)
            targetParticipant.roles.add(newRole)

            participantService.save(targetParticipant)
        } else if (status == RequestState.REJECTED) {
            request.rejectionReason = rejectionReason
        }

        return DomainResult.Success(requestRepository.save(request))
    }

    @Transactional
    fun createTeamCreateRequest(
        request: es.uib.tfg.sportsapi.dto.TeamCreateRequest
    ): DomainResult<TeamCreateRequest, ResolveRequestError> {

        val league = when(val result = leagueService.findLeagueById(request.leagueId)) {
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> result.data
        }

        val participant = when(val result = participantService.findParticipantById(request.participantId)) {
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> result.data
        }

        val teamCreateRequest = TeamCreateRequest(
            league = league,
            participant = participant,
            status = RequestState.PENDING,
            name = request.name,
            initials = request.initials,
            description = request.description,
            motto = request.motto,
            primaryColor = request.primaryColor,
            secondaryColor = request.secondaryColor,
            iconImageUrl = request.iconImageUrl.toString()
        )

        return DomainResult.Success(requestRepository.save(teamCreateRequest))
    }

    @Transactional
    fun resolveTeamCreateRequest(
        requestId: Long,
        status: RequestState,
        rejectionReason: String?
    ): DomainResult<TeamCreateRequest, ResolveRequestError> {

        val request = requestRepository.findByIdOrNull(requestId) as? TeamCreateRequest
                        ?: return DomainResult.Failure(RequestNotFound)

        if (request.status != RequestState.PENDING)
            return DomainResult.Failure(InvalidRequestState)

        request.status = status
        request.resolvedAt = LocalDateTime.now()

        if (status == RequestState.ACCEPTED) {
            val targetParticipant = request.participant

            when(val result = teamService.createTeamWithRequest(request, request.league)) {
                is DomainResult.Failure -> {
                    return DomainResult.Failure(CouldNotCreateTeam)
                }
                is DomainResult.Success -> {
                    targetParticipant.team = result.data
                }
            }

            val captainRole = participationRoleService.findRoleByName("CAPTAIN")
            val newRole = ParticipantRole(participant = targetParticipant, participationRole = captainRole)
            targetParticipant.roles.add(newRole)

            participantService.save(targetParticipant)
        } else if (status == RequestState.REJECTED) {
            request.rejectionReason = rejectionReason
        }

        return DomainResult.Success(requestRepository.save(request))
    }

    @Transactional
    fun createTeamJoinRequest(
        request: es.uib.tfg.sportsapi.dto.TeamJoinRequest
    ): DomainResult<TeamJoinRequest, CreateRequestError> {
        val team = when(val result = teamService.findById(request.teamId)) {
            is DomainResult.Failure -> return DomainResult.Failure(TeamNotFound)
            is DomainResult.Success -> {
                result.data
            }
        }

        val participant = when(val result = participantService.findParticipantById(request.participantId)) {
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> result.data
        }

        if(team.league != participant.league)
            return DomainResult.Failure(ParticipantAndTeamLeagueMissmatch)

        val teamJoinRequest = TeamJoinRequest(
            league = team.league,
            participant = participant,
            status = RequestState.PENDING,
            team = team,
            way = request.way,
        )
        return DomainResult.Success(requestRepository.save(teamJoinRequest))
    }

    @Transactional
    fun resolveTeamJoinRequest(
        requestId: Long,
        status: RequestState,
        userId: Long
    ): DomainResult<TeamJoinRequest, ResolveRequestError> {

        val request = requestRepository.findByIdOrNull(requestId) as? TeamJoinRequest
                        ?: return DomainResult.Failure(RequestNotFound)

        val resolver = when(val result = participantService
            .findParticipantByUserIdAndLeagueId(userId, request.league.id!!)) {
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> result.data
        }

        val requestTeam = request.team
        val isTeamCaptain = resolver.team == requestTeam && resolver.isCaptain()
        if (!isTeamCaptain)
            return DomainResult.Failure(UnauthorizedAction)

        if (request.status != RequestState.PENDING)
            return DomainResult.Failure(InvalidRequestState)

        request.status = status
        request.resolvedAt = LocalDateTime.now()

        if (status == RequestState.ACCEPTED) {
            val targetParticipant = request.participant
            targetParticipant.team = requestTeam

            participantService.save(targetParticipant)
        }

        return DomainResult.Success(requestRepository.save(request))
    }

    fun findJoinRequestsByTeamId(
        teamId: Long,
        userId: Long
    ): DomainResult<List<TeamJoinRequest>, RetrieveRequestError> {

        val team = when(val result = teamService.findById(teamId)) {
            is DomainResult.Failure -> return DomainResult.Failure(TeamNotFound)
            is DomainResult.Success -> result.data
        }

        val viewer = when(val result = participantService
            .findParticipantByUserIdAndLeagueId(userId, team.league.id!!)) {
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> result.data
        }

        val isTeamCaptain = viewer.team?.id == teamId && viewer.isCaptain()
        if (!isTeamCaptain)
            return DomainResult.Failure(UnauthorizedAction)

        val requests = teamJoinRequestRepository.findAllByTeamId(teamId)
        return DomainResult.Success(requests)
    }
}
