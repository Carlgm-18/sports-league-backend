package es.uib.tfg.sports_league_backend.league.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.domain.LeagueConfiguration
import es.uib.tfg.sports_league_backend.league.domain.PunctuationSystem
import es.uib.tfg.sports_league_backend.league.domain.errors.AlreadyJoin
import es.uib.tfg.sports_league_backend.league.domain.errors.CategoryMismatch
import es.uib.tfg.sports_league_backend.league.domain.errors.ConfigurationNotFound
import es.uib.tfg.sports_league_backend.league.domain.errors.InscriptionClosed
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueAlreadyEnded
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueCreateError
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueJoinError
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueNotFound
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueRetrieveError
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueStartError
import es.uib.tfg.sports_league_backend.league.domain.errors.PunctuationSystemNotFound
import es.uib.tfg.sports_league_backend.league.domain.errors.SportNotFound
import es.uib.tfg.sports_league_backend.league.domain.errors.UserNotFound
import es.uib.tfg.sports_league_backend.league.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueConfigurationRepository
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueRepository
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.PunctuationSystemRepository
import es.uib.tfg.sports_league_backend.participant.application.ParticipantService
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.errors.AlreadyParticipant
import es.uib.tfg.sports_league_backend.phase.application.PhaseService
import es.uib.tfg.sports_league_backend.sport.application.SportService
import es.uib.tfg.sports_league_backend.user.application.UserService
import es.uib.tfg.sports_league_backend.phase.domain.ClassificationPhase
import es.uib.tfg.sports_league_backend.phase.infrastructure.repository.PhaseRepository
import es.uib.tfg.sports_league_backend.round.infrastructure.repository.RoundRepository
import es.uib.tfg.sports_league_backend.match.infrastructure.repository.MatchRepository
import es.uib.tfg.sports_league_backend.team.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueUpdateError
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueNotFoundForUpdate
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueInProgressDateUpdate
import es.uib.tfg.sports_league_backend.league.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sportsapi.dto.*
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class LeagueService(
    private val leagueRepository: LeagueRepository,
    private val leagueConfigurationRepository: LeagueConfigurationRepository,
    private val punctuationSystemRepository: PunctuationSystemRepository,
    private val sportService: SportService,
    private val userService: UserService,
    private val participantService: ParticipantService,
    private val phaseService: PhaseService,
    private val phaseRepository: PhaseRepository,
    private val roundRepository: RoundRepository,
    private val matchRepository: MatchRepository
) {
    fun findAll(): List<League> =
        leagueRepository.findAll()

    @Transactional
    fun createLeague(request: LeagueCreateRequest, ownerId: Long): DomainResult<League, LeagueCreateError> {
        // Check for user existence
        val userResult = userService.findUserById(ownerId)
        val user = (userResult as? DomainResult.Success)?.data
            ?: return DomainResult.Failure(UserNotFound)

        // Validate configuration state
        val configResult = getOrCreateConfiguration(request)
        val configuration = (configResult as? DomainResult.Success)?.data
            ?: return DomainResult.Failure((configResult as DomainResult.Failure).error)

        // Validate punctuation system state
        val punctuationSystemResult = getOrCreatePunctuationSystem(request)
        val punctuationSystem = (punctuationSystemResult as? DomainResult.Success)?.data
            ?: return DomainResult.Failure((punctuationSystemResult as DomainResult.Failure).error)

        // Save league
        val league = request.toEntity(configuration, punctuationSystem, user)
        val savedLeague = leagueRepository.save(league)

        // Register creator as owner
        participantService.registerOwner(user, savedLeague)

        return DomainResult.Success(savedLeague)
    }

    private fun getOrCreateConfiguration(request: LeagueCreateRequest): DomainResult<LeagueConfiguration, LeagueCreateError> {
        return when {
            request.configurationId != null -> {
                leagueConfigurationRepository.findByIdOrNull(request.configurationId)
                    ?.let { DomainResult.Success(it) }
                    ?: DomainResult.Failure(ConfigurationNotFound)
            }
            request.customConfiguration != null -> {
                val sportResult = sportService.getSportById(request.customConfiguration.sportId)
                (sportResult as? DomainResult.Success)?.data?.let { sport ->
                    val newConfig = request.customConfiguration.toEntity(sport)
                    DomainResult.Success(leagueConfigurationRepository.save(newConfig))
                } ?: DomainResult.Failure(SportNotFound)
            }
            else -> throw IllegalStateException("Request must have either configurationId or customConfiguration")
        }
    }

    private fun getOrCreatePunctuationSystem(request: LeagueCreateRequest): DomainResult<PunctuationSystem, LeagueCreateError> {
        return when {
            request.punctuationSystemId != null -> {
                punctuationSystemRepository.findByIdOrNull(request.punctuationSystemId)
                    ?.let { DomainResult.Success(it) }
                    ?: DomainResult.Failure(PunctuationSystemNotFound)
            }
            request.customPunctuationSystem != null -> {
                val sportResult = sportService.getSportById(request.customPunctuationSystem.sportId)
                (sportResult as? DomainResult.Success)?.data?.let { sport ->
                    val newSystem = request.customPunctuationSystem.toEntity(sport)
                    DomainResult.Success(punctuationSystemRepository.save(newSystem))
                } ?: DomainResult.Failure(SportNotFound)
            }
            else -> throw IllegalStateException("Request must have either punctuationSystemId or customPunctuationSystem")
        }
    }

    fun findLeagueById(leagueId: Long): DomainResult<League, LeagueRetrieveError> =
        leagueRepository.findByIdOrNull(leagueId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(LeagueNotFound)

    @Transactional
    fun joinLeague(leagueId: Long, userId: Long): DomainResult<Participant, LeagueJoinError> {
        // Validate user existence
        val userResult = userService.findUserById(userId)
        val user = (userResult as? DomainResult.Success)?.data
            ?: return DomainResult.Failure(UserNotFound)

        // Validate league existence
        val league = leagueRepository.findByIdOrNull(leagueId)
            ?: return DomainResult.Failure(LeagueNotFound)

        // Validate league restrictions
        // 1.Category
        if(
            league.configuration.category != LeagueCategory.MIXT
            && league.configuration.category.value != user.category.value
        ) return DomainResult.Failure(CategoryMismatch)

        // 2. Max inscription date
        league.maxInscriptionDate?.let {
            if(LocalDate.now().isAfter(league.maxInscriptionDate))
                return DomainResult.Failure(InscriptionClosed)
        }

        // 3. League already ended
        if(league.status == LeagueState.ENDED)
            return DomainResult.Failure(LeagueAlreadyEnded)

        // Try to register player
        return when(val savedParticipant = participantService.registerPlayer(user, league)) {
            is DomainResult.Success -> {
                DomainResult.Success(savedParticipant.data)
            }

            is DomainResult.Failure -> {
                when(savedParticipant.error) {
                    AlreadyParticipant ->
                        DomainResult.Failure(AlreadyJoin)
                }
            }
        }

    }

    @Transactional
    fun updateConfiguration(
        leagueId: Long,
        request: ConfigurationUpdateRequest
    ): ConfigurationDetails {
        val league = leagueRepository.findByIdOrNull(leagueId)
            ?: throw IllegalArgumentException("League not found")
        val config = league.configuration
        request.minTeamFemaleIntegrants?.let { config.minTeamFemaleIntegrants = it }
        request.minTeamMembers?.let { config.minTeamMembers = it }
        request.maxTeamMembers?.let { config.maxTeamMembers = it }
        request.roundDuration?.let { config.roundDuration = it }
        val savedLeague = leagueRepository.save(league)
        return savedLeague.configuration.toDetailsDTO()
    }

    @Transactional
    fun updateLeague(leagueId: Long, request: LeagueUpdateRequest): DomainResult<League, LeagueUpdateError> {
        val league = leagueRepository.findByIdOrNull(leagueId)
            ?: return DomainResult.Failure(LeagueNotFoundForUpdate)

        if (league.status == LeagueState.IN_PROGRESS || league.status == LeagueState.ENDED) {
            if (request.startDate != null || request.endDate != null || request.maxInscriptionDate != null) {
                return DomainResult.Failure(LeagueInProgressDateUpdate)
            }
        }

        request.name?.let { league.name = it }
        request.description?.let { league.description = it }
        request.iconImageUrl?.let { league.iconImageUrl = it.toString() }
        request.bannerImageUrl?.let { league.bannerImageUrl = it.toString() }
        request.locationUrl?.let { league.locationUrl = it.toString() }
        request.startDate?.let { league.startDate = it }
        request.endDate?.let { league.endDate = it }
        request.maxInscriptionDate?.let { league.maxInscriptionDate = it }

        val saved = leagueRepository.save(league)
        return DomainResult.Success(saved)
    }

    @Transactional
    }

    fun findAllParticipantsByLeagueId(leagueId: Long): DomainResult<List<Participant>, LeagueRetrieveError> {
        return leagueRepository.findByIdOrNull(leagueId)
        ?.let { DomainResult.Success(participantService.findAllLeagueParticipants(leagueId)) }
        ?: DomainResult.Failure(LeagueNotFound)
    }

    @Transactional
    fun startLeague(leagueId: Long): DomainResult<Unit, LeagueStartError> {
        val league = leagueRepository.findByIdOrNull(leagueId)
            ?: return DomainResult.Failure(LeagueNotFound)

        league.status = LeagueState.IN_PROGRESS

        return when(val result = phaseService.generateIncomingPhaseMatches(leagueId)) {
            is DomainResult.Failure ->
                when(result.error) {
                    else -> throw IllegalStateException("League without phases")
                }

            is DomainResult.Success ->
                DomainResult.Success(result.data)
        }
    }
}