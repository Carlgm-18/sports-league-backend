package es.uib.tfg.sports_league_backend.phase.application

import es.uib.tfg.sports_league_backend.phase.domain.Phase
import es.uib.tfg.sports_league_backend.phase.infrastructure.repository.PhaseRepository
import org.springframework.stereotype.Service

@Service
class PhaseService(
    private val phaseRepository: PhaseRepository
) {
    fun findAllByLeagueId(leagueId: Int): List<Phase> {
        return phaseRepository.findAllByLeagueId(leagueId)
    }

    fun save(phase: Phase) {
        phaseRepository.save(phase)
    }

    fun findById(phaseId: Int): Phase {
        return phaseRepository.findById(phaseId)
            .orElseThrow { NoSuchElementException("Phase with id $phaseId not found") }
    }

}