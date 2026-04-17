package es.uib.tfg.sports_league_backend.user

import es.uib.tfg.sports_league_backend.common.exceptions.UserAlreadyExistsException
import es.uib.tfg.sports_league_backend.common.security.JwtService
import es.uib.tfg.sports_league_backend.user.entities.User
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

    @Transactional // Asegura que si algo falla, la base de datos hace un rollback y no guarda a medias
    fun registerUser(request: UserCreateRequest) {

        if (userRepository.existsByEmail(request.email)) {
            throw UserAlreadyExistsException("El email ${request.email} ya está registrado.")
        }

        val newUser = User(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password)!!,
            category = request.category
        )

        // 4. Guardar en PostgreSQL
        userRepository.save(newUser)

        // (Opcional) Aquí podrías guardar las licencias en otra tabla si las hay en el request
    }

    fun login(request: UserLoginRequest): UserAuthResponse {

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
}