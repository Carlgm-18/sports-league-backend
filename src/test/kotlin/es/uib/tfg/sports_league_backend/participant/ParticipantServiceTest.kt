package es.uib.tfg.sports_league_backend.participant

import es.uib.tfg.sports_league_backend.availability.application.AvailabilityService
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueRepository
import es.uib.tfg.sports_league_backend.participant.application.ParticipantService
import es.uib.tfg.sports_league_backend.participant.application.ports.out.ParticipationRoleRepositoryPort
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantRepository
import es.uib.tfg.sports_league_backend.participant.domain.ParticipationRole
import es.uib.tfg.sports_league_backend.participant.domain.errors.AlreadyParticipant
import es.uib.tfg.sports_league_backend.user.application.ports.`in`.FindUserUseCase
import es.uib.tfg.sports_league_backend.user.domain.User
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParticipantServiceTest {

    private val participantJPARepository = mockk<ParticipantRepository>()
    private val participationRoleRepository = mockk<ParticipationRoleRepositoryPort>()
    private val leagueRepository = mockk<LeagueRepository>()
    private val findUserUseCase = mockk<FindUserUseCase>()
    private val availabilityService = mockk<AvailabilityService>()

    private val participantService = ParticipantService(
        participantJPARepository = participantJPARepository,
        participationRoleRepository = participationRoleRepository,
        leagueRepository = leagueRepository,
        findUserUseCase = findUserUseCase,
        availabilityService = availabilityService
    )

    @Test
    fun `registerOwner should create participant with ADMIN role`() {
        // Given
        val user = mockk<User>()
        val league = mockk<League>()
        every { league.id } returns 1L
        every { user.id } returns 1L
        
        val adminRole = ParticipationRole(1L, "ADMIN")
        every { participationRoleRepository.findByRoleName("ADMIN") } returns adminRole
        every { participantJPARepository.save(any()) } answers { firstArg() }

        // When
        participantService.registerOwner(user, league)

        // Then
        verify(exactly = 1) { participationRoleRepository.findByRoleName("ADMIN") }
        verify(exactly = 1) { participantJPARepository.save(any()) }
    }

    @Test
    fun `registerPlayer should succeed when user is not already participant`() {
        // Given
        val user = mockk<User>()
        val league = mockk<League>()
        every { league.id } returns 5L
        every { user.id } returns 10L
        every { participantJPARepository.existsParticipant(5L, 10L) } returns false

        val playerRole = ParticipationRole(2L, "PLAYER")
        every { participationRoleRepository.findByRoleName("PLAYER") } returns playerRole
        every { participantJPARepository.save(any()) } answers { firstArg() }

        // When
        val result = participantService.registerPlayer(user, league)

        // Then
        assertTrue(result is DomainResult.Success)
        verify(exactly = 1) { participantJPARepository.existsParticipant(5L, 10L) }
        verify(exactly = 1) { participantJPARepository.save(any()) }
    }

    @Test
    fun `registerPlayer should fail when user is already participant`() {
        // Given
        val user = mockk<User>()
        val league = mockk<League>()
        every { league.id } returns 5L
        every { user.id } returns 10L
        every { participantJPARepository.existsParticipant(5L, 10L) } returns true

        // When
        val result = participantService.registerPlayer(user, league)

        // Then
        assertTrue(result is DomainResult.Failure)
        assertEquals(AlreadyParticipant, (result as DomainResult.Failure).error)
        verify(exactly = 0) { participantJPARepository.save(any()) }
    }
}
