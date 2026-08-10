package es.uib.tfg.sports_league_backend.league.infrastructure.repository

import es.uib.tfg.sports_league_backend.league.application.ports.out.PunctuationSystemRepositoryPort
import es.uib.tfg.sports_league_backend.league.domain.PunctuationSystem
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class PunctuationSystemPersistenceAdapter(
    private val punctuationSystemRepository: PunctuationSystemRepository
) : PunctuationSystemRepositoryPort {

    override fun findById(id: Long): PunctuationSystem? {
        return punctuationSystemRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun save(system: PunctuationSystem): PunctuationSystem {
        val entity = system.toJPAEntity()
        val saved = punctuationSystemRepository.save(entity)
        return saved.toDomain()
    }
}
