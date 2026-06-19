package es.uib.tfg.sports_league_backend.phase.infrastructure.repository

import es.uib.tfg.sports_league_backend.phase.domain.TournamentSlot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TournamentSlotRepository : JpaRepository<TournamentSlot, Long> {
    fun findByPhaseIdAndIndexOrder(phaseId: Long, indexOrder: Int): TournamentSlot?
    fun findByMatchId(matchId: Long): TournamentSlot?
}
