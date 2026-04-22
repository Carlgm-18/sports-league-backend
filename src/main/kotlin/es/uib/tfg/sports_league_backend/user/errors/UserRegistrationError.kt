package es.uib.tfg.sports_league_backend.user.errors

sealed interface UserRegistrationError {
    data class EmailAlreadyExists(val email: String) : UserRegistrationError
    object PasswordEncodingFailed : UserRegistrationError
    // En el futuro puedes añadir más sin romper nada:
    // object PasswordTooWeak : UserRegistrationError
    // object InvalidCategory : UserRegistrationError
}