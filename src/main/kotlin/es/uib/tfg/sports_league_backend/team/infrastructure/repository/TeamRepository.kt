package es.uib.tfg.sports_league_backend.team.infrastructure.repository

import es.uib.tfg.sports_league_backend.team.domain.Team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamRepository : JpaRepository<Team, Long> {
    fun findAllByLeagueId(leagueId: Long): List<Team>
    fun findAllByLeagueIdAndDeletedAtIsNull(leagueId: Long): List<Team>
}