package es.uib.tfg.sports_league_backend.common.security

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.application.ports.`in`.ManageLeagueUseCase
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.ManageParticipantUseCase
import org.springframework.stereotype.Component

@Component("leagueSecurityGuard")
class LeagueSecurityGuard(
    private val manageLeagueUseCase: ManageLeagueUseCase,
    private val manageParticipantUseCase: ManageParticipantUseCase
) {
    fun isAdmin(userId: Long, leagueId: Long): Boolean {
        val league = manageLeagueUseCase.findLeagueById(leagueId)
            .let { (it as? DomainResult.Success)?.data } ?: return false
        if (league.owner.id == userId) return true
        return manageParticipantUseCase.isLeagueParticipantAndHasRole(userId, leagueId, "ADMIN")
    }
}
