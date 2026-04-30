package es.uib.tfg.sports_league_backend.user.domain.errors

sealed interface UserRegisterError {
    object EmailAlreadyExists : UserRegisterError
    object PasswordEncodingFailed : UserRegisterError
    object PasswordsDontMatch : UserRegisterError
    // En el futuro puedes añadir más sin romper nada:
    // object PasswordTooWeak : UserRegistrationError
    // object InvalidCategory : UserRegistrationError
}