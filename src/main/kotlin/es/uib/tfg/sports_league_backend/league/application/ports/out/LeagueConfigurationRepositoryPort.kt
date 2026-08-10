package es.uib.tfg.sports_league_backend.league.application.ports.out

import es.uib.tfg.sports_league_backend.league.domain.LeagueConfiguration

interface LeagueConfigurationRepositoryPort {
    fun findById(id: Long): LeagueConfiguration?
    fun save(config: LeagueConfiguration): LeagueConfiguration
}
