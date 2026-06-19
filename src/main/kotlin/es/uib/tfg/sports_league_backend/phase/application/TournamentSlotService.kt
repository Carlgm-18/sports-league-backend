package es.uib.tfg.sports_league_backend.phase.application

import es.uib.tfg.sports_league_backend.phase.domain.TournamentSlot
import es.uib.tfg.sports_league_backend.phase.infrastructure.repository.TournamentSlotRepository
import org.springframework.stereotype.Service
import jakarta.transaction.Transactional

@Service
class TournamentSlotService(
    private val tournamentSlotRepository: TournamentSlotRepository
) {
    @Transactional
    fun save(slot: TournamentSlot): TournamentSlot =
        tournamentSlotRepository.save(slot)

    @Transactional
    fun saveAll(slots: List<TournamentSlot>): List<TournamentSlot> =
        tournamentSlotRepository.saveAll(slots)

    fun findByMatchId(matchId: Long): TournamentSlot? =
        tournamentSlotRepository.findByMatchId(matchId)

    fun findByPhaseIdAndIndexOrder(phaseId: Long, indexOrder: Int): TournamentSlot? =
        tournamentSlotRepository.findByPhaseIdAndIndexOrder(phaseId, indexOrder)
}
