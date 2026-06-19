package es.uib.tfg.sports_league_backend.result.infrastructure.repository

import es.uib.tfg.sports_league_backend.result.domain.Lineup
import es.uib.tfg.sports_league_backend.result.domain.Result
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LineupRepository : JpaRepository<Lineup, Long> {
    fun deleteByResult(result: Result)
}
