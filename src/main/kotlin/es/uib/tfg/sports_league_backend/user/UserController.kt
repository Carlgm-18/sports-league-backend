package es.uib.tfg.sports_league_backend.user

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.errors.UserLoginError
import es.uib.tfg.sports_league_backend.user.errors.UserRegistrationError
import es.uib.tfg.sportsapi.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    private val userService: UserService // Inyección del servicio
) {

    @PostMapping("/api/v1/users/register")
    fun register(@RequestBody @Valid request: UserCreateRequest): ResponseEntity<*> {

        return when (val result = userService.registerUser(request)) {

            // Éxito: 201 Created y devolvemos solo el DTO del usuario
            is DomainResult.Success -> {
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.data.toCreateResponse())
            }

            // Fallo: Evaluamos la clase sellada y devolvemos el error HTTP correspondiente
            is DomainResult.Failure -> {
                when (result.error) {
                    // Error 409: Conflict, el usuario ya existe
                    is UserRegistrationError.EmailAlreadyExists -> {
                        ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(mapOf("error" to "El email ${request.email} ya está registrado"))
                    }

                    // Error 500: Ha ocurrido un problema al intentar encriptar la contraseña
                    UserRegistrationError.PasswordEncodingFailed -> {
                        ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(mapOf("error" to "Error procesando la seguridad de la cuenta"))
                    }
                }
            }
        }
    }

    @PostMapping("/api/v1/auth/login")
    fun login(@Valid @RequestBody request: UserLoginRequest): UserAuthResponse {
//        return userService.login(request)
        TODO("Not yet implemented")
    }

    data class UserId(val id: Int)
    @GetMapping("/api/v1/users/me")
    fun getCurrentUser(@RequestBody request: UserId): ResponseEntity<*> {
        // El ID del usuario se sacará del token JWT en el futuro
        return when (val user = userService.getUserById(request.id)) {
            is DomainResult.Success -> {
                ResponseEntity
                        .status(HttpStatus.FOUND)
                        .body(user.data.toDetailsDTO())
            }

            is DomainResult.Failure -> {
                when (user.error) {
                    is UserLoginError.UserNotFound -> {
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(user.error)
                    }

                    else -> {
                        ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error inesperado")
                    }
                }
            }
        }
    }

    @PatchMapping("/api/v1/users/me")
    fun updateCurrentUser(@Valid @RequestBody request: UserUpdateRequest): UserDetails {
        TODO("Not yet implemented")
    }
}