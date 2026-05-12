package es.uib.tfg.sports_league_backend.phase.infrastructure.repository

import es.uib.tfg.sports_league_backend.phase.domain.Phase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PhaseRepository: JpaRepository<Phase, Int> {
    fun findAllByLeagueId(leagueId: Int): List<Phase>
}