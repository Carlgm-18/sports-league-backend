package es.uib.tfg.sports_league_backend.user.application

import es.uib.tfg.sports_league_backend.common.security.JwtService
import es.uib.tfg.sports_league_backend.common.security.TokenType
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.application.ports.`in`.FindUserUseCase
import es.uib.tfg.sports_league_backend.user.application.ports.`in`.LoginUserUseCase
import es.uib.tfg.sports_league_backend.user.application.ports.`in`.RegisterUserUseCase
import es.uib.tfg.sports_league_backend.user.application.ports.`in`.UpdateUserUseCase
import es.uib.tfg.sports_league_backend.user.application.ports.out.UserRepositoryPort
import es.uib.tfg.sports_league_backend.user.application.login.LoginSessionInfo
import es.uib.tfg.sports_league_backend.user.application.login.UserLoginCommand
import es.uib.tfg.sports_league_backend.user.application.register.UserRegisterCommand
import es.uib.tfg.sports_league_backend.user.application.update.UserUpdateCommand
import es.uib.tfg.sports_league_backend.user.domain.SecurePassword
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sports_league_backend.user.domain.errors.*
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepositoryPort,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    @Value($$"${JWT_EXPIRATION}") private val jwtExpirationMs: Long
) : RegisterUserUseCase, LoginUserUseCase, UpdateUserUseCase, FindUserUseCase {

    @Transactional
    override fun registerUser(command: UserRegisterCommand): DomainResult<User, UserRegisterError> {

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

    override fun login(command: UserLoginCommand): DomainResult<LoginSessionInfo, UserLoginError> {

        val user = userRepository.findByEmail(command.email)
        if (
            user == null ||
            !passwordEncoder.matches(command.password, user.passwordHash.value)
        ) {
            return DomainResult.Failure(NotValidCredentials)
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

    override fun findUserById(id: Long): DomainResult<User, UserRetrieveError> =
        userRepository.findById(id)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(UserNotFound)

    @Transactional
    override fun updateUserById(id: Long, newUser: UserUpdateCommand): DomainResult<User, UserRetrieveError> {

        val user = userRepository.findById(id)
            ?: return DomainResult.Failure(UserNotFound)

        newUser.firstName?.let{ user.firstName = it }
        newUser.lastName?.let{ user.lastName = it }
        newUser.licenses?.let{
            user.licenses.clear()
            user.licenses.addAll(it.map { l -> l.toEntity() })
        }

        return DomainResult.Success(userRepository.save(user))
    }

}