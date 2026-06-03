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
    private val leagueRepository: LeagueRepository,
    private val participantRepository: ParticipantRepository,
    private val participationRoleRepository: ParticipationRoleRepository,
    private val teamRepository: TeamRepository
) {

    private fun Participant.isAdmin(): Boolean =
        roles.any { it.participationRole.roleName == "ADMIN" }

    private fun Participant.isCaptain(): Boolean =
        roles.any { it.participationRole.roleName == "CAPTAIN" }

    @Transactional
    fun createRefereeRequest(leagueId: Long, userId: Long): DomainResult<RefereeRequest, RequestError> {
        val league = leagueRepository.findByIdOrNull(leagueId)
                        ?: return DomainResult.Failure(LeagueNotFound)

        val participant = participantRepository.findByUserIdAndLeagueId(userId, leagueId)
                            ?: return DomainResult.Failure(ParticipantNotFound)

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
            val refereeRole = participationRoleRepository.findByRoleName("REFEREE")
            if (targetParticipant.roles.none { it.participationRole.roleName == "REFEREE" }) {
                val newRole = ParticipantRole(participant = targetParticipant, participationRole = refereeRole)
                targetParticipant.roles.add(newRole)
                participantRepository.save(targetParticipant)
            }
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

        val league = leagueRepository.findByIdOrNull(leagueId)
                        ?: return DomainResult.Failure(LeagueNotFound)

        val participant = participantRepository.findByUserIdAndLeagueId(userId, leagueId)
                            ?: return DomainResult.Failure(ParticipantNotFound)

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
            val league = request.league

            // TODO: abstraer la logica de creacion en el TeamService
            val team = Team(
                league = league,
                name = request.name,
                initials = request.initials,
                description = request.description,
                motto = request.motto,
                primaryColor = request.primaryColor,
                secondaryColor = request.secondaryColor,
                iconImageUrl = request.iconImageUrl
            )
            val savedTeam = teamRepository.save(team)


            // Make the requesting participant the team CAPTAIN
            targetParticipant.team = savedTeam
            val captainRole = participationRoleRepository.findByRoleName("CAPTAIN")
            if (targetParticipant.roles.none { it.participationRole.roleName == "CAPTAIN" }) {
                val newRole = ParticipantRole(participant = targetParticipant, participationRole = captainRole)
                targetParticipant.roles.add(newRole)
            }
            participantRepository.save(targetParticipant)
        } else if (status == RequestState.REJECTED) {
            request.rejectionReason = rejectionReason
        }

        return DomainResult.Success(requestRepository.save(request) as TeamCreateRequest)
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

        // Check permission: Captain of the target team or Admin of the league
        val isTeamCaptain = resolver.team?.id == teamId && resolver.isCaptain()
        val isAdmin = resolver.isAdmin()
        if (!isTeamCaptain && !isAdmin) return DomainResult.Failure(UnauthorizedAction)

        val request = requestRepository.findByIdOrNull(requestId) as? TeamJoinRequest ?: return DomainResult.Failure(RequestNotFound)
        if (request.status != RequestState.PENDING) return DomainResult.Failure(InvalidRequestState)

        request.status = status
        request.resolvedAt = LocalDateTime.now()

        if (status == RequestState.ACCEPTED) {
            val targetParticipant = request.participant
            targetParticipant.team = team

            // Assign PLAYER role to the participant if they don't have it
            val playerRole = participationRoleRepository.findByRoleName("PLAYER")
            if (targetParticipant.roles.none { it.participationRole.roleName == "PLAYER" }) {
                val newRole = ParticipantRole(participant = targetParticipant, participationRole = playerRole)
                targetParticipant.roles.add(newRole)
            }
            participantRepository.save(targetParticipant)
        }

        return DomainResult.Success(requestRepository.save(request) as TeamJoinRequest)
    }

    fun findJoinRequestsByTeamId(teamId: Long, userId: Long): DomainResult<List<TeamJoinRequest>, RequestError> {
        val team = teamRepository.findByIdOrNull(teamId) ?: return DomainResult.Failure(TeamNotFound)
        val league = team.league
        val viewer = participantRepository.findByUserIdAndLeagueId(userId, league.id!!) ?: return DomainResult.Failure(ParticipantNotFound)

        // Check permission: Only team captain or league admin can list join requests
        val isTeamCaptain = viewer.team?.id == teamId && viewer.isCaptain()
        val isAdmin = viewer.isAdmin()
        if (!isTeamCaptain && !isAdmin) return DomainResult.Failure(UnauthorizedAction)

        val requests = teamJoinRequestRepository.findAllByTeamId(teamId)
        return DomainResult.Success(requests)
    }
}
