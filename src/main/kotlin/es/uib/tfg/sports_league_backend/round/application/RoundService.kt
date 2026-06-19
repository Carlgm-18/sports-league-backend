package es.uib.tfg.sports_league_backend.round.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.round.domain.Round
import es.uib.tfg.sports_league_backend.round.domain.errors.RoundNotFound
import es.uib.tfg.sports_league_backend.round.domain.errors.RoundRetrieveError
import es.uib.tfg.sports_league_backend.round.infrastructure.repository.RoundRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import jakarta.transaction.Transactional

@Service
class RoundService(
    private val roundRepository: RoundRepository
) {
    @Transactional
    fun save(round: Round): Round =
        roundRepository.save(round)

    @Transactional
    fun saveAll(rounds: List<Round>): List<Round> =
        roundRepository.saveAll(rounds)

    fun findRoundById(roundId: Long): DomainResult<Round, RoundRetrieveError> =
        roundRepository.findByIdOrNull(roundId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(RoundNotFound)
}
