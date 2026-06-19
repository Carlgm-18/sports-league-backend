package es.uib.tfg.sports_league_backend.common.security

import es.uib.tfg.sports_league_backend.match.infrastructure.repository.MatchRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component("matchSecurityGuard")
class MatchSecurityGuard(
    private val matchRepository: MatchRepository
) {
    fun isFirstReferee(userId: Long, matchId: Long): Boolean {
        val match = matchRepository.findByIdOrNull(matchId) ?: return false
        return match.firstReferee?.user?.id == userId
    }
}
