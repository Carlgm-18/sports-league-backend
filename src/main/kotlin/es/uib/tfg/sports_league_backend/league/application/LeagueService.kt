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
import es.uib.tfg.sports_league_backend.league.application.ports.`in`.ManageLeagueUseCase
import es.uib.tfg.sports_league_backend.league.application.ports.`in`.ManageLeagueConfigurationUseCase
import es.uib.tfg.sports_league_backend.league.application.ports.`in`.LeagueQueryUseCase
import es.uib.tfg.sports_league_backend.league.application.ports.`in`.JoinLeagueUseCase
import es.uib.tfg.sports_league_backend.league.application.ports.out.LeagueRepositoryPort
import es.uib.tfg.sports_league_backend.league.application.ports.out.LeagueConfigurationRepositoryPort
import es.uib.tfg.sports_league_backend.league.application.ports.out.PunctuationSystemRepositoryPort
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.RegisterParticipantUseCase
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.ManageParticipantUseCase
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.errors.AlreadyParticipant
import es.uib.tfg.sports_league_backend.phase.application.PhaseService
import es.uib.tfg.sports_league_backend.sport.application.SportService
import es.uib.tfg.sports_league_backend.user.application.ports.`in`.FindUserUseCase
import es.uib.tfg.sports_league_backend.phase.domain.ClassificationPhase
import es.uib.tfg.sports_league_backend.phase.infrastructure.repository.PhaseRepository
import es.uib.tfg.sports_league_backend.round.infrastructure.repository.RoundRepository
import es.uib.tfg.sports_league_backend.match.infrastructure.repository.MatchRepository
import es.uib.tfg.sports_league_backend.team.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueUpdateError
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueNotFoundForUpdate
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueInProgressDateUpdate
import es.uib.tfg.sports_league_backend.league.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.phase.infrastructure.repository.ClassificationGroupRepository
import es.uib.tfg.sports_league_backend.match.application.MatchService
import es.uib.tfg.sports_league_backend.match.domain.Match
import es.uib.tfg.sports_league_backend.match.domain.Proposal
import es.uib.tfg.sportsapi.dto.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class LeagueService(
    private val leagueRepository: LeagueRepositoryPort,
    private val leagueConfigurationRepository: LeagueConfigurationRepositoryPort,
    private val punctuationSystemRepository: PunctuationSystemRepositoryPort,
    private val sportService: SportService,
    private val findUserUseCase: FindUserUseCase,
    private val registerParticipantUseCase: RegisterParticipantUseCase,
    private val manageParticipantUseCase: ManageParticipantUseCase,
    private val phaseService: PhaseService,
    private val phaseRepository: PhaseRepository,
    private val roundRepository: RoundRepository,
    private val matchRepository: MatchRepository,
    private val classificationGroupRepository: ClassificationGroupRepository,
    private val matchService: MatchService
) : ManageLeagueUseCase, ManageLeagueConfigurationUseCase, LeagueQueryUseCase, JoinLeagueUseCase {
    override fun findAll(): List<League> =
        leagueRepository.findAll()

    @Transactional
    override fun createLeague(request: LeagueCreateRequest, ownerId: Long): DomainResult<League, LeagueCreateError> {
        val userResult = findUserUseCase.findUserById(ownerId)
        val user = (userResult as? DomainResult.Success)?.data
            ?: return DomainResult.Failure(UserNotFound)

        val configResult = getOrCreateConfiguration(request)
        val configuration = (configResult as? DomainResult.Success)?.data
            ?: return DomainResult.Failure((configResult as DomainResult.Failure).error)

        val punctuationSystemResult = getOrCreatePunctuationSystem(request)
        val punctuationSystem = (punctuationSystemResult as? DomainResult.Success)?.data
            ?: return DomainResult.Failure((punctuationSystemResult as DomainResult.Failure).error)

        val league = request.toEntity(configuration, punctuationSystem, user)
        val savedLeague = leagueRepository.save(league)

        registerParticipantUseCase.registerOwner(user, savedLeague)

        return DomainResult.Success(savedLeague)
    }

    private fun getOrCreateConfiguration(request: LeagueCreateRequest): DomainResult<LeagueConfiguration, LeagueCreateError> {
        return when {
            request.configurationId != null -> {
                leagueConfigurationRepository.findById(request.configurationId)
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
                punctuationSystemRepository.findById(request.punctuationSystemId)
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

    override fun findLeagueById(leagueId: Long): DomainResult<League, LeagueRetrieveError> =
        leagueRepository.findById(leagueId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(LeagueNotFound)

    @Transactional
    override fun joinLeague(leagueId: Long, userId: Long): DomainResult<Participant, LeagueJoinError> {
        val userResult = findUserUseCase.findUserById(userId)
        val user = (userResult as? DomainResult.Success)?.data
            ?: return DomainResult.Failure(UserNotFound)

        val league = leagueRepository.findById(leagueId)
            ?: return DomainResult.Failure(LeagueNotFound)

        if(
            league.configuration.category != LeagueCategory.MIXT
            && league.configuration.category.value != user.category.value
        ) return DomainResult.Failure(CategoryMismatch)

        league.maxInscriptionDate?.let {
            if(LocalDate.now().isAfter(league.maxInscriptionDate))
                return DomainResult.Failure(InscriptionClosed)
        }

        if(league.status == LeagueState.ENDED)
            return DomainResult.Failure(LeagueAlreadyEnded)

        return when(val savedParticipant = registerParticipantUseCase.registerPlayer(user, league)) {
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
    override fun updateConfiguration(
        leagueId: Long,
        request: ConfigurationUpdateRequest
    ): ConfigurationDetails {
        val league = leagueRepository.findById(leagueId)
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
    override fun updateLeague(leagueId: Long, request: LeagueUpdateRequest): DomainResult<League, LeagueUpdateError> {
        val league = leagueRepository.findById(leagueId)
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
    override fun getLeaderboard(
        leagueId: Long,
        phaseId: Long?,
        roundId: Long?
    ): DomainResult<List<LeaderboardGroup>, LeagueRetrieveError> {
        val league = leagueRepository.findById(leagueId)
            ?: return DomainResult.Failure(LeagueNotFound)

        val selectedPhase = if (phaseId != null) {
            phaseRepository.findById(phaseId).orElse(null)
                ?: return DomainResult.Failure(LeagueNotFound)
        } else if (roundId != null) {
            val round = roundRepository.findById(roundId).orElse(null)
                ?: return DomainResult.Failure(LeagueNotFound)
            round.phase
        } else {
            val active = phaseRepository.findActivePhase(leagueId, LocalDate.now())
            active as? ClassificationPhase
                ?: phaseRepository.findAllByLeagueIdOrderBySequenceOrder(leagueId)
                    .filterIsInstance<ClassificationPhase>()
                    .firstOrNull()
        }

        if (selectedPhase !is ClassificationPhase) {
            return DomainResult.Success(emptyList())
        }

        val targetRound = if (roundId != null) roundRepository.findById(roundId).orElse(null) else null
        val roundSequenceOrder = targetRound?.sequenceOrder ?: Int.MAX_VALUE

        val leaderboardGroups = selectedPhase.groups.map { group ->
            val groupTeamsMap = group.teams.associateBy { it.id }
            val projections = classificationGroupRepository.getGroupLeaderboard(group.id!!, selectedPhase.id!!, roundSequenceOrder)

            val standings = projections.map { proj ->
                val team = groupTeamsMap[proj.getTeamId()] ?: throw IllegalStateException("Team not found in group")
                LeaderboardRow(
                    position = 0,
                    team = team.toSummaryDTO(),
                    playedMatches = proj.getPlayedMatches(),
                    wonMatches = proj.getWonMatches(),
                    lostMatches = proj.getLostMatches(),
                    drawnMatches = proj.getDrawnMatches(),
                    points = proj.getPoints(),
                    wonSets = proj.getWonSets(),
                    lostSets = proj.getLostSets(),
                    wonPoints = proj.getWonPoints(),
                    lostPoints = proj.getLostPoints()
                )
            }

            val sortedStandings = standings.sortedWith(
                compareByDescending<LeaderboardRow> { it.points ?: 0 }
                    .thenByDescending { (it.wonSets ?: 0) - (it.lostSets ?: 0) }
                    .thenByDescending { (it.wonPoints ?: 0) - (it.lostPoints ?: 0) }
            ).mapIndexed { index, row ->
                row.copy(position = index + 1)
            }

            LeaderboardGroup(
                groupId = group.id ?: 0L,
                groupName = group.name,
                standings = sortedStandings
            )
        }

        return DomainResult.Success(leaderboardGroups)
    }

    override fun findAllParticipantsByLeagueId(leagueId: Long): DomainResult<List<Participant>, LeagueRetrieveError> {
        return leagueRepository.findById(leagueId)
        ?.let { DomainResult.Success(manageParticipantUseCase.findAllLeagueParticipants(leagueId)) }
        ?: DomainResult.Failure(LeagueNotFound)
    }

    @Transactional
    override fun startLeague(leagueId: Long): DomainResult<Unit, LeagueStartError> {
        val league = leagueRepository.findById(leagueId)
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

    override fun getActiveProposal(matchId: Long): Proposal? {
        return matchService.getActiveProposal(matchId)
    }

    override fun findMatches(
        leagueId: Long,
        phaseId: Long?,
        teamId: Long?,
        status: MatchState?
    ): List<Match> {
        val matches = if (phaseId != null) {
            matchRepository.findAllByPhaseId(phaseId)
        } else {
            matchRepository.findAllByLeagueId(leagueId)
        }

        return matches.filter { match ->
            val matchesTeam = teamId == null || match.localTeam?.id == teamId || match.visitorTeam?.id == teamId
            val matchesStatus = status == null || match.status == status
            matchesTeam && matchesStatus
        }
    }
}