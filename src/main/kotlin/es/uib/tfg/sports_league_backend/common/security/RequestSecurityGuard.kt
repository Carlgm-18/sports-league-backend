package es.uib.tfg.sports_league_backend.common.security

import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.ManageParticipantUseCase
import es.uib.tfg.sports_league_backend.request.domain.RefereeRequest
import es.uib.tfg.sports_league_backend.request.domain.TeamCreateRequest
import es.uib.tfg.sports_league_backend.request.domain.TeamJoinRequest
import es.uib.tfg.sports_league_backend.request.infrastructure.repository.RequestRepository
import es.uib.tfg.sports_league_backend.team.infrastructure.repository.TeamRepository
import es.uib.tfg.sportsapi.dto.BaseRequest
import es.uib.tfg.sportsapi.dto.RefereeRequest as RefereeRequestDTO
import es.uib.tfg.sportsapi.dto.TeamCreateRequest as TeamCreateRequestDTO
import es.uib.tfg.sportsapi.dto.TeamJoinRequest as TeamJoinRequestDTO
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component("requestSecurityGuard")
class RequestSecurityGuard(
    private val requestRepository: RequestRepository,
    private val teamRepository: TeamRepository,
    private val manageParticipantUseCase: ManageParticipantUseCase,
    private val leagueSecurityGuard: LeagueSecurityGuard
) {
    fun canCreateRequest(userId: Long, leagueId: Long, request: BaseRequest): Boolean {
        return when (request) {
            is RefereeRequestDTO -> {
                val participant = manageParticipantUseCase.findParticipantById(request.participantId)
                    .let { (it as? es.uib.tfg.sports_league_backend.core.DomainResult.Success)?.data } ?: return false
                participant.user.id == userId && participant.league.id == leagueId
            }
            is TeamCreateRequestDTO -> {
                val participant = manageParticipantUseCase.findParticipantById(request.participantId)
                    .let { (it as? es.uib.tfg.sports_league_backend.core.DomainResult.Success)?.data } ?: return false
                participant.user.id == userId && participant.league.id == leagueId
            }
            is TeamJoinRequestDTO -> {
                val team = teamRepository.findByIdOrNull(request.teamId) ?: return false
                if (team.league.id != leagueId) return false

                if (request.way == TeamJoinRequestDTO.Way.INVITATION) {
                    // Current user must be the team captain of the target team in the league
                    val senderParticipant = manageParticipantUseCase.findParticipant(userId, leagueId)
                        .let { (it as? es.uib.tfg.sports_league_backend.core.DomainResult.Success)?.data } ?: return false
                    senderParticipant.team?.id == team.id && senderParticipant.roles.any { it.participationRole.roleName == "CAPTAIN" }
                } else { // APPLIANCE
                    // Current user must be the participant applying to the team
                    val participant = manageParticipantUseCase.findParticipantById(request.participantId)
                        .let { (it as? es.uib.tfg.sports_league_backend.core.DomainResult.Success)?.data } ?: return false
                    participant.user.id == userId && participant.league.id == leagueId
                }
            }
            else -> false
        }
    }

    fun canResolveRequest(userId: Long, requestId: Long): Boolean {
        val request = requestRepository.findByIdOrNull(requestId) ?: return false
        val leagueId = request.league.id!!

        return when (request) {
            is RefereeRequest -> {
                // Only league admins can resolve referee requests
                leagueSecurityGuard.isAdmin(userId, leagueId)
            }
            is TeamCreateRequest -> {
                // Only league admins can resolve team creation requests
                leagueSecurityGuard.isAdmin(userId, leagueId)
            }
            is TeamJoinRequest -> {
                if (request.way == TeamJoinRequestDTO.Way.INVITATION) {
                    // Invited player must resolve the request, so current user must be that participant
                    request.participant.user.id == userId
                } else { // APPLIANCE
                    // Team captain must resolve the request
                    val resolverParticipant = manageParticipantUseCase.findParticipant(userId, leagueId)
                        .let { (it as? es.uib.tfg.sports_league_backend.core.DomainResult.Success)?.data } ?: return false
                    resolverParticipant.team?.id == request.team.id && resolverParticipant.roles.any { it.participationRole.roleName == "CAPTAIN" }
                }
            }
            else -> false
        }
    }

    fun canViewJoinRequests(userId: Long, teamId: Long): Boolean {
        val team = teamRepository.findByIdOrNull(teamId) ?: return false
        val viewer = manageParticipantUseCase.findParticipant(userId, team.league.id!!)
            .let { (it as? es.uib.tfg.sports_league_backend.core.DomainResult.Success)?.data } ?: return false
        return viewer.team?.id == teamId && viewer.roles.any { it.participationRole.roleName == "CAPTAIN" }
    }

    fun canViewRequest(userId: Long, requestId: Long): Boolean {
        val request = requestRepository.findByIdOrNull(requestId) ?: return false
        val leagueId = request.league.id!!

        if (request.participant.user.id == userId) return true

        return when (request) {
            is RefereeRequest -> leagueSecurityGuard.isAdmin(userId, leagueId)
            is TeamCreateRequest -> leagueSecurityGuard.isAdmin(userId, leagueId)
            is TeamJoinRequest -> {
                val viewer = manageParticipantUseCase.findParticipant(userId, leagueId)
                    .let { (it as? es.uib.tfg.sports_league_backend.core.DomainResult.Success)?.data } ?: return false
                viewer.team?.id == request.team.id && viewer.roles.any { it.participationRole.roleName == "CAPTAIN" }
            }
            else -> false
        }
    }
}
