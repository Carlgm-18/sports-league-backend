package es.uib.tfg.sports_league_backend.league.infrastructure.repository

import es.uib.tfg.sports_league_backend.league.application.ports.out.LeagueConfigurationRepositoryPort
import es.uib.tfg.sports_league_backend.league.domain.LeagueConfiguration
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class LeagueConfigurationPersistenceAdapter(
    private val leagueConfigurationRepository: LeagueConfigurationRepository
) : LeagueConfigurationRepositoryPort {

    override fun findById(id: Long): LeagueConfiguration? {
        return leagueConfigurationRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun save(config: LeagueConfiguration): LeagueConfiguration {
        val entity = config.toJPAEntity()
        val saved = leagueConfigurationRepository.save(entity)
        return saved.toDomain()
    }
}
