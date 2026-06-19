package es.uib.tfg.sports_league_backend.phase.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.phase.domain.Phase
import es.uib.tfg.sports_league_backend.phase.domain.ClassificationPhase
import es.uib.tfg.sports_league_backend.phase.domain.TournamentPhase
import es.uib.tfg.sports_league_backend.phase.domain.errors.HandlePhaseError
import es.uib.tfg.sports_league_backend.phase.domain.errors.LastPhaseError
import es.uib.tfg.sports_league_backend.phase.domain.errors.PhaseNotFound
import es.uib.tfg.sports_league_backend.phase.domain.errors.PhaseRetrieveError
import es.uib.tfg.sports_league_backend.phase.infrastructure.repository.PhaseRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PhaseService(
    private val phaseRepository: PhaseRepository,
    private val classificationStrategy: ClassificationMatchGenerationStrategy,
    private val tournamentStrategy: TournamentMatchGenerationStrategy
) {
    fun findAllByLeagueId(leagueId: Long): List<Phase> =
        phaseRepository.findAllByLeagueIdOrderBySequenceOrder(leagueId)

    fun save(phase: Phase) =
        phaseRepository.save(phase)


    fun findById(phaseId: Long): DomainResult<Phase, PhaseRetrieveError> =
        phaseRepository.findByIdOrNull(phaseId)
            ?.let{ DomainResult.Success(it) }
            ?: DomainResult.Failure(PhaseNotFound)

    @Transactional
    fun generateIncomingPhaseMatches(leagueId: Long): DomainResult<Unit, HandlePhaseError> {
        val phase = phaseRepository.findIncomingPhase(leagueId, LocalDate.now())
            ?: return DomainResult.Failure(LastPhaseError)
        
        val strategy = when (phase) {
            is ClassificationPhase -> classificationStrategy
            is TournamentPhase -> tournamentStrategy
            else -> throw IllegalArgumentException("Unknown phase type: ${phase::class.simpleName}")
        }

        return DomainResult.Success(strategy.generate(phase))
    }
}