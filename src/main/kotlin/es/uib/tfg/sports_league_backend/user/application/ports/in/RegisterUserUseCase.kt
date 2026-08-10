package es.uib.tfg.sports_league_backend.user.application.ports.`in`

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.application.register.UserRegisterCommand
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sports_league_backend.user.domain.errors.UserRegisterError

interface RegisterUserUseCase {
    fun registerUser(command: UserRegisterCommand): DomainResult<User, UserRegisterError>
}
