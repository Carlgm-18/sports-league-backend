package es.uib.tfg.sports_league_backend.league.infrastructure.repository

import es.uib.tfg.sports_league_backend.league.application.ports.out.LeagueRepositoryPort
import es.uib.tfg.sports_league_backend.league.domain.League
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class LeaguePersistenceAdapter(
    private val leagueRepository: LeagueRepository
) : LeagueRepositoryPort {

    override fun findAll(): List<League> {
        return leagueRepository.findAllByDeletedAtIsNull().map { it.toDomain() }
    }

    override fun findById(id: Long): League? {
        return leagueRepository.findByIdAndDeletedAtIsNull(id)?.toDomain()
    }

    override fun save(league: League): League {
        val jpaEntity = league.toJPAEntity()
        val savedEntity = leagueRepository.save(jpaEntity)
        return savedEntity.toDomain()
    }

    override fun existsById(id: Long): Boolean {
        return leagueRepository.existsByIdAndDeletedAtIsNull(id)
    }
}
