package es.uib.tfg.sports_league_backend.user.application

import es.uib.tfg.sports_league_backend.common.security.JwtService
import es.uib.tfg.sports_league_backend.common.security.TokenType
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.domain.errors.UserNotFound
import es.uib.tfg.sports_league_backend.user.application.login.LoginSessionInfo
import es.uib.tfg.sports_league_backend.user.application.login.UserLoginCommand
import es.uib.tfg.sports_league_backend.user.application.register.UserRegisterCommand
import es.uib.tfg.sports_league_backend.user.application.update.UserUpdateCommand
import es.uib.tfg.sports_league_backend.user.domain.SecurePassword
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sports_league_backend.user.domain.errors.*
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.user.infrastructure.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    @Value($$"${JWT_EXPIRATION}") private val jwtExpirationMs: Long
) {

    @Transactional
    fun registerUser(command: UserRegisterCommand): DomainResult<User, UserRegisterError> {

        if(command.password != command.confirmPassword) {
            return DomainResult.Failure(PasswordsDontMatch)
        }

        if (userRepository.existsByEmail(command.email)) {
            return DomainResult.Failure(EmailAlreadyExists)
        }

        val encodedPassword = passwordEncoder.encode(command.password)
            ?: return DomainResult.Failure(PasswordEncodingFailed)

        val newUser = command.toEntity(SecurePassword(encodedPassword))

        val savedUser = userRepository.save(newUser)

        return DomainResult.Success(savedUser)
    }

    fun login(command: UserLoginCommand): DomainResult<LoginSessionInfo, UserLoginError> {

        val user = userRepository.findByEmail(command.email)
        if (
            user == null ||
            !passwordEncoder.matches(command.password, user.passwordHash.value)
        ) {
            return DomainResult.Failure(
                UserLoginError.NotValidCredentials(
                    command.email,
                    command.password
                )
            )
        }

        val accessToken = jwtService.generateToken(user.id!!, TokenType.ACCESS)
        val jwtRefreshToken = jwtService.generateToken(user.id!!, TokenType.REFRESH)

        return DomainResult.Success(
            LoginSessionInfo(
                accessToken,
                jwtRefreshToken,
                jwtExpirationMs,
                "Bearer",
                user
            )
        )
    }

    fun findUserById(id: Long): DomainResult<User, UserRetrieveError> =
        userRepository.findByIdOrNull(id)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(UserNotFound)

    fun updateUserById(id: Long, newUser: UserUpdateCommand): DomainResult<User, UserRetrieveError> {
        val user = userRepository.findByIdOrNull(id)
            ?: return DomainResult.Failure(UserNotFound)
        newUser.email?.let{ user.email = it }
        newUser.firstName?.let{ user.firstName = it }
        newUser.lastName?.let{ user.lastName = it }
        newUser.licenses?.let{ user.licenses = it }
        newUser.signature?.let{ user.signature = it }

        return DomainResult.Success(userRepository.save(user))
    }

}