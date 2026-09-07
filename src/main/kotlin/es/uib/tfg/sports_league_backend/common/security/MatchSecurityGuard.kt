package es.uib.tfg.sports_league_backend.common.security

import es.uib.tfg.sports_league_backend.match.infrastructure.repository.MatchRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component("matchSecurityGuard")
class MatchSecurityGuard(
    private val matchRepository: MatchRepository,
    private val leagueSecurityGuard: LeagueSecurityGuard
) {
    fun isFirstReferee(userId: Long, matchId: Long): Boolean {
        val match = matchRepository.findByIdOrNull(matchId) ?: return false
        return match.firstReferee?.user?.id == userId
    }

    fun isAdminOfMatch(userId: Long, matchId: Long): Boolean {
        val match = matchRepository.findByIdOrNull(matchId) ?: return false
        val leagueId = match.localTeam?.league?.id ?: match.visitorTeam?.league?.id ?: return false
        return leagueSecurityGuard.isAdmin(userId, leagueId)
    }
}
