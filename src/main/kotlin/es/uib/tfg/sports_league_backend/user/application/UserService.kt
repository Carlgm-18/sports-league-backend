package es.uib.tfg.sports_league_backend.user.application

import es.uib.tfg.sports_league_backend.common.security.JwtService
import es.uib.tfg.sports_league_backend.common.security.TokenType
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.domain.SecurePassword
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sports_league_backend.user.domain.errors.UserLoginError
import es.uib.tfg.sports_league_backend.user.domain.errors.UserRegisterError
import es.uib.tfg.sports_league_backend.user.domain.errors.UserRetrieveError
import es.uib.tfg.sports_league_backend.user.infrastructure.controller.UserController.UserId
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toSummary
import es.uib.tfg.sports_league_backend.user.infrastructure.repository.UserRepository
import es.uib.tfg.sportsapi.dto.UserAuthResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    //@Value($$"${jwt.expiration}") private val jwtExpirationMs: Long
) {

    @Transactional
    fun registerUser(command: UserRegisterCommand): DomainResult<User, UserRegisterError> {

        if(command.password != command.confirmPassword) {
            return DomainResult.Failure(UserRegisterError.PasswordsDontMatch)
        }

        if (userRepository.existsByEmail(command.email)) {
            return DomainResult.Failure(UserRegisterError.EmailAlreadyExists)
        }

        val encodedPassword = passwordEncoder.encode(command.password)
            ?: return DomainResult.Failure(UserRegisterError.PasswordEncodingFailed)

        val newUser = command.toEntity(SecurePassword(encodedPassword))

        val savedUser = userRepository.save(newUser)

        return DomainResult.Success(savedUser)
    }

    fun login(command: UserLoginCommand): DomainResult<LoginSessionInfo, UserLoginError> {

        val user = userRepository.findByEmail(command.email)
        println(user)
        println(String.format("Comparing %s, %s", passwordEncoder.encode(command.password), user?.passwordHash?.value))
        if (user == null || !passwordEncoder.matches(command.password, user.passwordHash.value)) {
            return DomainResult.Failure(
                UserLoginError.NotValidCredentials(
                    command.email,
                    command.password
                )
            )
        }

        val jwtToken = jwtService.generateToken(user.id, TokenType.ACCESS)
        val jwtRefreshToken = jwtService.generateToken(user.id, TokenType.REFRESH)

        return DomainResult.Success(
            LoginSessionInfo(
                accessToken = jwtToken,
                refreshToken = jwtRefreshToken,
                expiresIn = 3600,
                tokenType = "Bearer",
                user
            )
        )
    }

    // TODO: make that it uses a JWT instead of a userId directly
    fun getUserById(id: Int): DomainResult<User, UserRetrieveError> {
        val user = userRepository.findById(id)

        if (user.isEmpty) {
            return DomainResult.Failure(UserRetrieveError.UserNotFound(UserId(id)))
        }

        return DomainResult.Success(user.get())
    }
}