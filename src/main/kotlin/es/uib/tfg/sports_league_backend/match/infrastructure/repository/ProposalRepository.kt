package es.uib.tfg.sports_league_backend.match.infrastructure.repository

import es.uib.tfg.sports_league_backend.match.domain.Proposal
import es.uib.tfg.sportsapi.dto.ProposalState
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProposalRepository : JpaRepository<Proposal, Long> {
    fun findByMatchId(matchId: Long): List<Proposal>
    fun findFirstByMatchIdAndStatusOrderByProposedAtDesc(matchId: Long, status: ProposalState): Proposal?
}
