package es.uib.tfg.sports_league_backend.user

import es.uib.tfg.sports_league_backend.common.security.JwtService
import es.uib.tfg.sports_league_backend.common.security.TokenType
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.application.UserService
import es.uib.tfg.sports_league_backend.user.application.login.UserLoginCommand
import es.uib.tfg.sports_league_backend.user.application.ports.out.UserRepositoryPort
import es.uib.tfg.sports_league_backend.user.application.register.UserRegisterCommand
import es.uib.tfg.sports_league_backend.user.domain.SecurePassword
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sports_league_backend.user.domain.errors.*
import es.uib.tfg.sportsapi.dto.UserCategory
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

class UserServiceTest {

    private val userRepository = mockk<UserRepositoryPort>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val jwtService = mockk<JwtService>()
    private val jwtExpirationMs = 3600L

    private val userService = UserService(
        userRepository = userRepository,
        passwordEncoder = passwordEncoder,
        jwtService = jwtService,
        jwtExpirationMs = jwtExpirationMs
    )

    @Test
    fun `registerUser should succeed when details are valid`() {
        // Given
        val creationDate = LocalDateTime.now()

        val command = UserRegisterCommand(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            password = "password123",
            confirmPassword = "password123",
            category = UserCategory.MALE,
            createdAt = creationDate
        )
        every { userRepository.existsByEmail(command.email) } returns false
        every { passwordEncoder.encode(command.password) } returns "encoded_password"
        
        val expectedUser = User(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            passwordHash = SecurePassword("encoded_password"),
            category = UserCategory.MALE,
            createdAt = creationDate
        )
        every { userRepository.save(any()) } returns expectedUser

        // When
        val result = userService.registerUser(command)

        // Then
        assertTrue(result is DomainResult.Success)
        assertEquals(expectedUser, (result as DomainResult.Success).data)
        verify { userRepository.existsByEmail(command.email) }
        verify { passwordEncoder.encode(command.password) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `registerUser should fail when passwords do not match`() {
        // Given
        val command = UserRegisterCommand(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            password = "password123",
            confirmPassword = "different_password",
            category = UserCategory.MALE,
            createdAt = LocalDateTime.now()
        )

        // When
        val result = userService.registerUser(command)

        // Then
        assertTrue(result is DomainResult.Failure)
        assertEquals(PasswordsDontMatch, (result as DomainResult.Failure).error)
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `registerUser should fail when email already exists`() {
        // Given
        val command = UserRegisterCommand(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            password = "password123",
            confirmPassword = "password123",
            category = UserCategory.MALE,
            createdAt = LocalDateTime.now()
        )
        every { userRepository.existsByEmail(command.email) } returns true

        // When
        val result = userService.registerUser(command)

        // Then
        assertTrue(result is DomainResult.Failure)
        assertEquals(EmailAlreadyExists, (result as DomainResult.Failure).error)
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `login should succeed with valid credentials`() {
        // Given
        val command = UserLoginCommand(
            email = "john@example.com",
            password = "password123"
        )
        val user = User(
            id = 42L,
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            passwordHash = SecurePassword("encoded_password"),
            category = UserCategory.MALE,
            createdAt = LocalDateTime.now()
        )
        every { userRepository.findByEmail(command.email) } returns user
        every { passwordEncoder.matches(command.password, "encoded_password") } returns true
        every { jwtService.generateToken(42L, TokenType.ACCESS) } returns "access_token"
        every { jwtService.generateToken(42L, TokenType.REFRESH) } returns "refresh_token"

        // When
        val result = userService.login(command)

        // Then
        assertTrue(result is DomainResult.Success)
        val data = (result as DomainResult.Success).data
        assertEquals("access_token", data.accessToken)
        assertEquals("refresh_token", data.refreshToken)
        assertEquals(user, data.user)
    }

    @Test
    fun `login should fail with invalid credentials`() {
        // Given
        val command = UserLoginCommand(
            email = "john@example.com",
            password = "wrong_password"
        )
        every { userRepository.findByEmail(command.email) } returns null

        // When
        val result = userService.login(command)

        // Then
        assertTrue(result is DomainResult.Failure)
        assertEquals(NotValidCredentials, (result as DomainResult.Failure).error)
    }

    @Test
    fun `findUserById should return user when exists`() {
        // Given
        val user = User(
            id = 10L,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            passwordHash = SecurePassword("hash"),
            category = UserCategory.MALE,
            createdAt = LocalDateTime.now()
        )
        every { userRepository.findById(10L) } returns user

        // When
        val result = userService.findUserById(10L)

        // Then
        assertTrue(result is DomainResult.Success)
        assertEquals(user, (result as DomainResult.Success).data)
    }

    @Test
    fun `findUserById should fail when user does not exist`() {
        // Given
        every { userRepository.findById(10L) } returns null

        // When
        val result = userService.findUserById(10L)

        // Then
        assertTrue(result is DomainResult.Failure)
        assertEquals(UserNotFound, (result as DomainResult.Failure).error)
    }
}
