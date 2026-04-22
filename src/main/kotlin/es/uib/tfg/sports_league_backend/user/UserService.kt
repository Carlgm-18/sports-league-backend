package es.uib.tfg.sports_league_backend.user

import es.uib.tfg.sports_league_backend.common.exceptions.UserAlreadyExistsException
import es.uib.tfg.sports_league_backend.common.security.JwtService
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.entities.User
import es.uib.tfg.sports_league_backend.user.errors.UserLoginError
import es.uib.tfg.sports_league_backend.user.errors.UserRegistrationError
import es.uib.tfg.sportsapi.dto.UserAuthResponse
import es.uib.tfg.sportsapi.dto.UserCreateRequest
import es.uib.tfg.sportsapi.dto.UserLoginRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    //@Value($$"${jwt.expiration}") private val jwtExpirationMs: Long
) {

    @Transactional
    fun registerUser(request: UserCreateRequest): DomainResult<User, UserRegistrationError> {

        if (userRepository.existsByEmail(request.email)) {
            return DomainResult.Failure(UserRegistrationError.EmailAlreadyExists(request.email))
        }

        val encodedPassword = passwordEncoder.encode(request.password)
            ?: return DomainResult.Failure(UserRegistrationError.PasswordEncodingFailed)

        val newUser = request.toEntity(encodedPassword)

        val savedUser = userRepository.save(newUser)

        return DomainResult.Success(savedUser)
    }

    fun login(request: UserLoginRequest): User {

//        val user = userRepository.findByEmail(request.email) ?: throw RuntimeException("Credenciales inválidas")
//
//        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
//            throw RuntimeException("Credenciales inválidas")
//        }
//
//        val jwtToken = jwtService.generateToken(user.email, user.id)
//
//        val userDetails = user.toDetailsDTO()
//
//        return UserAuthResponse(
//            accessToken = jwtToken,
//            // Generamos un identificador único aleatorio para el Refresh Token por ahora
//            refreshToken = UUID.randomUUID().toString(),
//            // Convertimos los milisegundos a segundos para el front
//            expiresIn = (jwtExpirationMs / 1000).toInt(), tokenType = "Bearer", user = userDetails
//        )
        TODO("Not yet implemented")
    }

    fun getUserById(id: Int): DomainResult<User, UserLoginError> {
        val user = userRepository.findById(id)

        if (user.isEmpty) {
            return DomainResult.Failure(UserLoginError.UserNotFound(id))
        }

        return DomainResult.Success(user.get())
    }
}