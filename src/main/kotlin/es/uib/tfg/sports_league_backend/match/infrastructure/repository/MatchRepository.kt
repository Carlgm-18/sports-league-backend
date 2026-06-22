package es.uib.tfg.sports_league_backend.match.infrastructure.repository

import es.uib.tfg.sports_league_backend.match.domain.Match
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MatchRepository : JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m, Round r WHERE m.roundId = r.id AND r.phase.league.id = :leagueId")
    fun findAllByLeagueId(@Param("leagueId") leagueId: Long): List<Match>

    @Query("SELECT m FROM Match m, Round r WHERE m.roundId = r.id AND r.phase.id = :phaseId")
    fun findAllByPhaseId(@Param("phaseId") phaseId: Long): List<Match>
}