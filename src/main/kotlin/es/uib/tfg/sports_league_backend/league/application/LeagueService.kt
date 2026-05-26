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
        // Check for user existence
        val userResult = userService.findUserById(ownerId)
        val user = (userResult as? Success)?.data
            ?: return Failure(UserNotFound)

        // Validate configuration state
        val configResult = getOrCreateConfiguration(request)
        val configuration = (configResult as? Success)?.data
            ?: return Failure((configResult as Failure).error)

        // Validate punctuation system state
        val punctuationSystemResult = getOrCreatePunctuationSystem(request)
        val punctuationSystem = (punctuationSystemResult as? Success)?.data
            ?: return Failure((punctuationSystemResult as Failure).error)

        // Save league
        val league = request.toEntity(configuration, punctuationSystem, user)
        val savedLeague = leagueRepository.save(league)

        // Register creator as owner
        participantService.registerOwner(user, savedLeague)

        return Success(savedLeague)
    }

    private fun getOrCreateConfiguration(request: LeagueCreateRequest): DomainResult<LeagueConfiguration, LeagueCreateError> {
        return when {
            request.configurationId != null -> {
                leagueConfigurationRepository.findByIdOrNull(request.configurationId)
                    ?.let { Success(it) }
                    ?: Failure(ConfigurationNotFound)
            }
            request.customConfiguration != null -> {
                val sportResult = sportService.getSportById(request.customConfiguration.sportId)
                (sportResult as? Success)?.data?.let { sport ->
                    val newConfig = request.customConfiguration.toEntity(sport)
                    Success(leagueConfigurationRepository.save(newConfig))
                } ?: Failure(SportNotFound)
            }
            else -> throw IllegalStateException("Request must have either configurationId or customConfiguration")
        }
    }

    private fun getOrCreatePunctuationSystem(request: LeagueCreateRequest): DomainResult<PunctuationSystem, LeagueCreateError> {
        return when {
            request.punctuationSystemId != null -> {
                punctuationSystemRepository.findByIdOrNull(request.punctuationSystemId)
                    ?.let { Success(it) }
                    ?: Failure(PunctuationSystemNotFound)
            }
            request.customPunctuationSystem != null -> {
                val sportResult = sportService.getSportById(request.customPunctuationSystem.sportId)
                (sportResult as? Success)?.data?.let { sport ->
                    val newSystem = request.customPunctuationSystem.toEntity(sport)
                    Success(punctuationSystemRepository.save(newSystem))
                } ?: Failure(SportNotFound)
            }
            else -> throw IllegalStateException("Request must have either punctuationSystemId or customPunctuationSystem")
        }
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