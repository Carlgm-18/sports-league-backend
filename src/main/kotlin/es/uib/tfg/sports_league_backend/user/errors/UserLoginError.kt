package es.uib.tfg.sports_league_backend.user.errors

sealed interface UserLoginError {
    data class PasswordNotMatch(val password: String) : UserLoginError
    data class UserNotFound(val id: Int) : UserLoginError
    // En el futuro puedes añadir más sin romper nada:
    // object PasswordTooWeak : UserRegistrationError
    // object InvalidCategory : UserRegistrationError
}