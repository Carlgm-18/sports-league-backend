package es.uib.tfg.sports_league_backend.user.domain.errors

sealed interface UserLoginError
sealed interface UserRegisterError
sealed interface UserRetrieveError

object UserNotFoundError : UserRetrieveError
data class NotValidCredentials(val email: String, val password: String): UserLoginError
object EmailAlreadyExists : UserRegisterError
object PasswordEncodingFailed : UserRegisterError
object PasswordsDontMatch : UserRegisterError