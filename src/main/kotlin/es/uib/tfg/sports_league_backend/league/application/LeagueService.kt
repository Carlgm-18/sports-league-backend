package es.uib.tfg.sports_league_backend.league.application

import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.domain.LeagueConfiguration
import es.uib.tfg.sports_league_backend.league.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueConfigurationRepository
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueRepository
import es.uib.tfg.sports_league_backend.punctuation.infrastructure.repository.PunctuationSystemRepository
import es.uib.tfg.sportsapi.dto.ConfigurationDetails
import es.uib.tfg.sportsapi.dto.ConfigurationUpdateRequest
import es.uib.tfg.sportsapi.dto.LeagueCreateRequest
import es.uib.tfg.sportsapi.dto.LeagueDetails
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class LeagueService(
    private val leagueRepository: LeagueRepository,
    private val leagueConfigurationRepository: LeagueConfigurationRepository,
    private val punctuationSystemRepository: PunctuationSystemRepository
) {
    fun findAll(): List<League> =
        leagueRepository.findAll()

    @Transactional
    fun createLeague(league: League): League {
        // TODO: Implement database logic
        // Save league, save phases, save configuration, save punctuation rules

        val config = league.configuration
        if(config.id == null) {
            leagueConfigurationRepository.save(config)
        }

        val punctuationSystem = league.punctuationSystem
        if(punctuationSystem.id == null) {
            punctuationSystemRepository.save(punctuationSystem)
        }

        return leagueRepository.save(league)
    }

    fun findById(leagueId: Int): League {
        // TODO: Implement database logic
        TODO("Service not yet implemented")
    }

    fun updateConfiguration(
        leagueId: Int,
        request: ConfigurationUpdateRequest
    ): ConfigurationDetails {
        // TODO: Implement database logic
        TODO("Not yet implemented")
    }

}