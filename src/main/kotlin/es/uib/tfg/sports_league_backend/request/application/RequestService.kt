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
import es.uib.tfg.sportsapi.dto.RefereeRequest as RefereeRequestDTO
import es.uib.tfg.sportsapi.dto.TeamCreateRequest as TeamCreateRequestDTO
import es.uib.tfg.sportsapi.dto.TeamJoinRequest as TeamJoinRequestDTO
import es.uib.tfg.sports_league_backend.request.domain.RefereeRequest
import es.uib.tfg.sports_league_backend.request.domain.TeamCreateRequest
import es.uib.tfg.sports_league_backend.request.domain.TeamJoinRequest
import es.uib.tfg.sportsapi.dto.ResolveRequestInput
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

    private fun isTeamCaptain(userId: Long, request: TeamJoinRequest): Boolean =
        when(
            val result = participantService
                .findParticipantByUserIdAndLeagueId(userId, request.league.id!!)
        ) {
            is DomainResult.Failure -> false

            is DomainResult.Success -> result.data.team == request.team && result.data.isCaptain()
        }

    private fun Participant.isAdmin(): Boolean =
        roles.any { it.participationRole.roleName == "ADMIN" }

    private fun isAdminFromSameLeague(userId: Long, request: Request): Boolean =
        when(
            val result = participantService
                            .findParticipantByUserIdAndLeagueId(userId, request.league.id!!)
        ) {
            is DomainResult.Failure -> false

            is DomainResult.Success -> result.data.isAdmin()
        }


    @Transactional
    fun createRefereeRequest(
        request: RefereeRequestDTO
    ): DomainResult<RefereeRequest, CreateRequestError> {
        val league = when(val result = leagueService.findLeagueById(request.leagueId)) {
            is DomainResult.Failure -> return DomainResult.Failure(LeagueNotFound)
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
    fun createTeamJoinRequest(
        request: TeamJoinRequestDTO
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
    fun createTeamCreateRequest(
        request: TeamCreateRequestDTO
    ): DomainResult<TeamCreateRequest, CreateRequestError> {

        val league = when(val result = leagueService.findLeagueById(request.leagueId)) {
            is DomainResult.Failure -> return DomainResult.Failure(LeagueNotFound)
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
    fun resolveRequest(
        requestId: Long,
        input: ResolveRequestInput,
        userId: Long
    ): DomainResult<Request, ResolveRequestError> {
        val request = requestRepository.findByIdOrNull(requestId)
            ?: return DomainResult.Failure(RequestNotFound)


        if (request.status != RequestState.PENDING)
            return DomainResult.Failure(InvalidRequestState)

        request.status = input.status
        request.resolvedAt = LocalDateTime.now()

        if (request.status == RequestState.ACCEPTED) {
            return when (request) {
                is TeamCreateRequest ->
                    resolveTeamCreateRequest(request, userId)

                is RefereeRequest ->
                    resolveRefereeRequest(request, userId)

                is TeamJoinRequest ->
                    resolveTeamJoinRequest(request, userId)

                else -> {
                    throw IllegalArgumentException("Unexpected type of request")
                }
            }
        }
        else if (request.status == RequestState.REJECTED) {
            input.rejectionReason?.let { request.rejectionReason = it }
        }

        return DomainResult.Success(request)

    }


    @Transactional
    fun resolveTeamCreateRequest(
        request: TeamCreateRequest,
        userId: Long
    ): DomainResult<TeamCreateRequest, ResolveRequestError> {

        when(val resolver = participantService
                            .findParticipantByUserIdAndLeagueId(userId, request.league.id!!)
        ){
            is DomainResult.Failure -> DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> {
                if(!resolver.data.isAdmin())
                    return DomainResult.Failure(UnauthorizedAction)

            }
        }

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

        return DomainResult.Success(requestRepository.save(request))
    }

    @Transactional
    fun resolveRefereeRequest(
        request: RefereeRequest,
        userId: Long
    ): DomainResult<RefereeRequest, ResolveRequestError> {

        when(val resolver = participantService
            .findParticipantByUserIdAndLeagueId(userId, request.league.id!!)
        ){
            is DomainResult.Failure -> DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> {
                if(!resolver.data.isAdmin())
                    return DomainResult.Failure(UnauthorizedAction)

            }
        }

        val targetParticipant = request.participant

        val refereeRole = participationRoleService.findRoleByName("REFEREE")
        val newRole = ParticipantRole(participant = targetParticipant, participationRole = refereeRole)
        targetParticipant.roles.add(newRole)

        participantService.save(targetParticipant)
        return DomainResult.Success(requestRepository.save(request))
    }

    @Transactional
    fun resolveTeamJoinRequest(
        request: TeamJoinRequest,
        userId: Long
    ): DomainResult<TeamJoinRequest, ResolveRequestError> {

        val resolver = when(
            val result = participantService
                            .findParticipantByUserIdAndLeagueId(userId, request.league.id!!)
        ) {
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
            is DomainResult.Success -> result.data
        }

        val requestTeam = request.team
        val isTeamCaptain = resolver.team == requestTeam && resolver.isCaptain()
        if (!isTeamCaptain)
            return DomainResult.Failure(UnauthorizedAction)

        val targetParticipant = request.participant
        targetParticipant.team = requestTeam

        participantService.save(targetParticipant)

        return DomainResult.Success(requestRepository.save(request))
    }

    fun findRequestByRequestId(
        requestId: Long,
        userId: Long
    ): DomainResult<Request, RetrieveRequestError> =
        requestRepository.findByIdOrNull(requestId)
            ?.let {
                return when(it) {
                    is RefereeRequest ->
                        findRequest(it, userId)
                    is TeamJoinRequest ->
                        findRequest(it, userId)
                    is TeamCreateRequest ->
                        findRequest(it, userId)

                    else -> throw IllegalArgumentException("Unexpected type of request")
                }
            }
            ?: DomainResult.Failure(RequestNotFound)

    private fun isRequestCreator(userId: Long, request: Request): Boolean =
        userId == request.participant.id


    private fun findRequest(
        request: TeamCreateRequest,
        userId: Long
    ): DomainResult<TeamCreateRequest, RetrieveRequestError> {
        return if(!isRequestCreator(userId, request) && !isAdminFromSameLeague(userId, request))
            DomainResult.Failure(UnauthorizedAction)
        else
            DomainResult.Success(request)
    }

    private fun findRequest(
        request: RefereeRequest,
        userId: Long
    ): DomainResult<RefereeRequest, RetrieveRequestError> {
        return if(!isRequestCreator(userId, request) && !isAdminFromSameLeague(userId, request))
            DomainResult.Failure(UnauthorizedAction)
        else
            DomainResult.Success(request)
    }

    private fun findRequest(
        request: TeamJoinRequest,
        userId: Long
    ): DomainResult<TeamJoinRequest, RetrieveRequestError> {
        return if(!isRequestCreator(userId, request) && !isTeamCaptain(userId, request))
            DomainResult.Failure(UnauthorizedAction)
        else
            DomainResult.Success(request)
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
