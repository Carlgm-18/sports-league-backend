package es.uib.tfg.sports_league_backend.common.security

import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueRepository
import es.uib.tfg.sports_league_backend.participant.application.ParticipantService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component("leagueSecurityGuard")
class LeagueSecurityGuard(
    private val leagueRepository: LeagueRepository,
    private val participantService: ParticipantService
) {
    fun isAdmin(userId: Long, leagueId: Long): Boolean {
        val league = leagueRepository.findByIdOrNull(leagueId) ?: return false
        if (league.owner.id == userId) return true
        return participantService.isLeagueParticipantAndHasRole(userId, leagueId, "ADMIN")
    }
}
