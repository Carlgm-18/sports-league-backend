package es.uib.tfg.sports_league_backend.phase.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.phase.domain.Phase
import es.uib.tfg.sports_league_backend.phase.domain.errors.PhaseNotFound
import es.uib.tfg.sports_league_backend.phase.domain.errors.PhaseRetrieveError
import es.uib.tfg.sports_league_backend.phase.infrastructure.repository.PhaseRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PhaseService(
    private val phaseRepository: PhaseRepository
) {
    fun findAllByLeagueId(leagueId: Long): List<Phase> =
        phaseRepository.findAllByLeagueIdOrderBySequenceOrder(leagueId)

    fun save(phase: Phase) =
        phaseRepository.save(phase)


    fun findById(phaseId: Long): DomainResult<Phase, PhaseRetrieveError> =
        phaseRepository.findByIdOrNull(phaseId)
            ?.let{ DomainResult.Success(it) }
            ?: DomainResult.Failure(PhaseNotFound)

}