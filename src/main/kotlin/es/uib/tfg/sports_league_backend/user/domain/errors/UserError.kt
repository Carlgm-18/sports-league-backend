package es.uib.tfg.sports_league_backend.user.domain.errors

sealed interface UserLoginError
sealed interface UserRegisterError
sealed interface UserRetrieveError

object UserNotFound : UserRetrieveError
object NotValidCredentials : UserLoginError
object EmailAlreadyExists : UserRegisterError
object PasswordEncodingFailed : UserRegisterError
object PasswordsDontMatch : UserRegisterError