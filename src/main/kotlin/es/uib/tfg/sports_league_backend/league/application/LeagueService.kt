package es.uib.tfg.sports_league_backend.league.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.domain.LeagueConfiguration
import es.uib.tfg.sports_league_backend.league.domain.PunctuationSystem
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueCreateError
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueRetrieveError
import es.uib.tfg.sports_league_backend.league.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueConfigurationRepository
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueRepository
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.PunctuationSystemRepository
import es.uib.tfg.sports_league_backend.sport.application.SportService
import es.uib.tfg.sports_league_backend.sport.domain.errors.SportRetrieveError
import es.uib.tfg.sports_league_backend.user.application.UserService
import es.uib.tfg.sportsapi.dto.ConfigurationDetails
import es.uib.tfg.sportsapi.dto.ConfigurationUpdateRequest
import es.uib.tfg.sportsapi.dto.LeagueCreateRequest
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class LeagueService(
    private val leagueRepository: LeagueRepository,
    private val leagueConfigurationRepository: LeagueConfigurationRepository,
    private val punctuationSystemRepository: PunctuationSystemRepository,
    private val sportService: SportService,
    private val userService: UserService,
) {
    fun findAll(): List<League> =
        leagueRepository.findAll()

    @Transactional
    fun createLeague(request: LeagueCreateRequest, ownerId: Long): DomainResult<League, LeagueCreateError> {

        val userEntity = when (val user = userService.findUserById(ownerId)) {
            is DomainResult.Success -> user.data
            is DomainResult.Failure -> return DomainResult.Failure(LeagueCreateError.UserNotFound)
        }

        val configurationEntity = when {
            request.configurationId != null -> {
                leagueConfigurationRepository.findByIdOrNull(request.configurationId)
                    ?: return DomainResult.Failure(LeagueCreateError.ConfigurationNotFound)
            }
            request.customConfiguration != null -> {
                val sportResult = sportService.getSportById(request.customConfiguration.sportId)
                if (sportResult is DomainResult.Failure) return DomainResult.Failure(LeagueCreateError.SportNotFound)

                val newConfig = request.customConfiguration.toEntity((sportResult as DomainResult.Success).data)
                leagueConfigurationRepository.save(newConfig)
            }
            else -> throw IllegalStateException("Unreachable code")
        }

        val punctuationSystemEntity = when {
            request.punctuationSystemId != null -> {
                punctuationSystemRepository.findByIdOrNull(request.punctuationSystemId)
                    ?: return DomainResult.Failure(LeagueCreateError.PunctuationSystemNotFound)
            }
            request.customPunctuationSystem != null -> {
                val sportResult = sportService.getSportById(request.customPunctuationSystem.sportId)
                if (sportResult is DomainResult.Failure) return DomainResult.Failure(LeagueCreateError.SportNotFound)

                val newSystem = request.customPunctuationSystem.toEntity((sportResult as DomainResult.Success).data)
                punctuationSystemRepository.save(newSystem)
            }
            else -> throw IllegalStateException("Unreachable code")
        }

        val league = request.toEntity(configurationEntity, punctuationSystemEntity, userEntity)
        val savedLeague = leagueRepository.save(league)

        return DomainResult.Success(savedLeague)
    }

    fun findLeagueById(leagueId: Long): DomainResult<League, LeagueRetrieveError> =
        leagueRepository.findByIdOrNull(leagueId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(LeagueRetrieveError.LeagueNotFound)

    fun updateConfiguration(
        leagueId: Int,
        request: ConfigurationUpdateRequest
    ): ConfigurationDetails {
        // TODO: Implement database logic
        TODO("Not yet implemented")
    }

}