package es.uib.tfg.sports_league_backend.request.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueRepository
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantRole
import es.uib.tfg.sportsapi.dto.TeamJoinRequest.Way
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipantRepository
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipationRoleRepository
import es.uib.tfg.sports_league_backend.request.domain.*
import es.uib.tfg.sports_league_backend.request.domain.errors.*
import es.uib.tfg.sports_league_backend.request.infrastructure.repository.RequestRepository
import es.uib.tfg.sports_league_backend.request.infrastructure.repository.TeamJoinRequestRepository
import es.uib.tfg.sports_league_backend.team.application.TeamService
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sports_league_backend.team.infrastructure.repository.TeamRepository
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

    private fun Participant.isAdmin(): Boolean =
        roles.any { it.participationRole.roleName == "ADMIN" }

    private fun Participant.isCaptain(): Boolean =
        roles.any { it.participationRole.roleName == "CAPTAIN" }

    @Transactional
    fun createRefereeRequest(leagueId: Long, userId: Long): DomainResult<RefereeRequest, RequestError> {
        val league = leagueRepository.findByIdOrNull(leagueId)
                        ?: return DomainResult.Failure(LeagueNotFound)
        val league = when(val result = leagueService.findLeagueById(request.participantId)) {
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> result.data

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
        leagueId: Long,
        requestId: Long,
        userId: Long,
        status: RequestState,
        rejectionReason: String?
    ): DomainResult<RefereeRequest, RequestError> {
        val resolver = participantRepository.findByUserIdAndLeagueId(userId, leagueId)
                        ?: return DomainResult.Failure(ParticipantNotFound)
        if (!resolver.isAdmin())
            return DomainResult.Failure(UnauthorizedAction)

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
        leagueId: Long,
        userId: Long,
        name: String,
        initials: String,
        description: String?,
        motto: String?,
        primaryColor: String,
        secondaryColor: String,
        iconImageUrl: String?
    ): DomainResult<TeamCreateRequest, RequestError> {

        val league = when(val result = leagueService.findLeagueById(request.participantId)) {
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
            name = name,
            initials = initials,
            description = description,
            motto = motto,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            iconImageUrl = iconImageUrl
        )

        return DomainResult.Success(requestRepository.save(teamCreateRequest))
    }

    @Transactional
    fun resolveTeamCreateRequest(
        leagueId: Long,
        requestId: Long,
        userId: Long,
        status: RequestState,
        rejectionReason: String?
    ): DomainResult<TeamCreateRequest, RequestError> {

        val resolver = participantRepository.findByUserIdAndLeagueId(userId, leagueId)
                        ?: return DomainResult.Failure(ParticipantNotFound)

        if (!resolver.isAdmin())
            return DomainResult.Failure(UnauthorizedAction)

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
        teamId: Long,
        userId: Long,
        way: Way
    ): DomainResult<TeamJoinRequest, RequestError> {
        val team = teamRepository.findByIdOrNull(teamId) ?: return DomainResult.Failure(TeamNotFound)
        val league = team.league
        val participant = participantRepository.findByUserIdAndLeagueId(userId, league.id!!) ?: return DomainResult.Failure(ParticipantNotFound)
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


        val teamJoinRequest = TeamJoinRequest(
            league = league,
            participant = participant,
            status = RequestState.PENDING,
            team = team,
            way = way
        )
        return DomainResult.Success(requestRepository.save(teamJoinRequest) as TeamJoinRequest)
    }

    @Transactional
    fun resolveTeamJoinRequest(
        teamId: Long,
        requestId: Long,
        userId: Long,
        status: RequestState
    ): DomainResult<TeamJoinRequest, RequestError> {
        val team = teamRepository.findByIdOrNull(teamId) ?: return DomainResult.Failure(TeamNotFound)
        val league = team.league
        val resolver = participantRepository.findByUserIdAndLeagueId(userId, league.id!!) ?: return DomainResult.Failure(ParticipantNotFound)
        val request = requestRepository.findByIdOrNull(requestId) as? TeamJoinRequest
                        ?: return DomainResult.Failure(RequestNotFound)

        val resolver = when(val result = participantService
            .findParticipantByUserIdAndLeagueId(userId, request.league.id!!)) {
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> result.data
        }

        val request = requestRepository.findByIdOrNull(requestId) as? TeamJoinRequest ?: return DomainResult.Failure(RequestNotFound)
        if (request.status != RequestState.PENDING) return DomainResult.Failure(InvalidRequestState)

        request.status = status
        request.resolvedAt = LocalDateTime.now()

        if (status == RequestState.ACCEPTED) {
            val targetParticipant = request.participant
            targetParticipant.team = team

            participantService.save(targetParticipant)
        }

        return DomainResult.Success(requestRepository.save(request) as TeamJoinRequest)
    }

    fun findJoinRequestsByTeamId(teamId: Long, userId: Long): DomainResult<List<TeamJoinRequest>, RequestError> {
        val team = teamRepository.findByIdOrNull(teamId) ?: return DomainResult.Failure(TeamNotFound)
        val league = team.league
        val viewer = participantRepository.findByUserIdAndLeagueId(userId, league.id!!) ?: return DomainResult.Failure(ParticipantNotFound)
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
        val isAdmin = viewer.isAdmin()
        if (!isTeamCaptain && !isAdmin) return DomainResult.Failure(UnauthorizedAction)

        val requests = teamJoinRequestRepository.findAllByTeamId(teamId)
        return DomainResult.Success(requests)
    }
}
