package es.uib.tfg.sports_league_backend.league

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.application.LeagueService
import es.uib.tfg.sports_league_backend.league.application.ports.out.LeagueConfigurationRepositoryPort
import es.uib.tfg.sports_league_backend.league.application.ports.out.LeagueRepositoryPort
import es.uib.tfg.sports_league_backend.league.application.ports.out.PunctuationSystemRepositoryPort
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueNotFound
import es.uib.tfg.sports_league_backend.match.application.MatchService
import es.uib.tfg.sports_league_backend.match.infrastructure.repository.MatchRepository
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.ManageParticipantUseCase
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.RegisterParticipantUseCase
import es.uib.tfg.sports_league_backend.phase.application.PhaseService
import es.uib.tfg.sports_league_backend.phase.infrastructure.repository.ClassificationGroupRepository
import es.uib.tfg.sports_league_backend.phase.infrastructure.repository.PhaseRepository
import es.uib.tfg.sports_league_backend.round.infrastructure.repository.RoundRepository
import es.uib.tfg.sports_league_backend.sport.application.SportService
import es.uib.tfg.sports_league_backend.user.application.ports.`in`.FindUserUseCase
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LeagueServiceTest {

    private val leagueRepository = mockk<LeagueRepositoryPort>()
    private val leagueConfigurationRepository = mockk<LeagueConfigurationRepositoryPort>()
    private val punctuationSystemRepository = mockk<PunctuationSystemRepositoryPort>()
    private val sportService = mockk<SportService>()
    private val findUserUseCase = mockk<FindUserUseCase>()
    private val registerParticipantUseCase = mockk<RegisterParticipantUseCase>()
    private val manageParticipantUseCase = mockk<ManageParticipantUseCase>()
    private val phaseService = mockk<PhaseService>()
    private val phaseRepository = mockk<PhaseRepository>()
    private val roundRepository = mockk<RoundRepository>()
    private val matchRepository = mockk<MatchRepository>()
    private val classificationGroupRepository = mockk<ClassificationGroupRepository>()
    private val matchService = mockk<MatchService>()

    private val leagueService = LeagueService(
        leagueRepository = leagueRepository,
        leagueConfigurationRepository = leagueConfigurationRepository,
        punctuationSystemRepository = punctuationSystemRepository,
        sportService = sportService,
        findUserUseCase = findUserUseCase,
        registerParticipantUseCase = registerParticipantUseCase,
        manageParticipantUseCase = manageParticipantUseCase,
        phaseService = phaseService,
        phaseRepository = phaseRepository,
        roundRepository = roundRepository,
        matchRepository = matchRepository,
        classificationGroupRepository = classificationGroupRepository,
        matchService = matchService
    )

    @Test
    fun `findLeagueById should succeed when league exists`() {
        // Given
        val expectedLeague = mockk<League>()
        every { leagueRepository.findById(10L) } returns expectedLeague

        // When
        val result = leagueService.findLeagueById(10L)

        // Then
        assertTrue(result is DomainResult.Success)
        assertEquals(expectedLeague, (result as DomainResult.Success).data)
        verify(exactly = 1) { leagueRepository.findById(10L) }
    }

    @Test
    fun `findLeagueById should fail when league does not exist`() {
        // Given
        every { leagueRepository.findById(10L) } returns null

        // When
        val result = leagueService.findLeagueById(10L)

        // Then
        assertTrue(result is DomainResult.Failure)
        assertEquals(LeagueNotFound, (result as DomainResult.Failure).error)
        verify(exactly = 1) { leagueRepository.findById(10L) }
    }
}
