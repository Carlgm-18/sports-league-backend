package es.uib.tfg.sports_league_backend.user.domain.errors

sealed interface UserLoginError {
    data class NotValidCredentials(val email: String, val password: String) : UserLoginError
    // En el futuro puedes añadir más sin romper nada:
    // object PasswordTooWeak : UserRegistrationError
    // object InvalidCategory : UserRegistrationError
}