package es.uib.tfg.sports_league_backend.user.application.ports.`in`

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.application.login.LoginSessionInfo
import es.uib.tfg.sports_league_backend.user.application.login.UserLoginCommand
import es.uib.tfg.sports_league_backend.user.domain.errors.UserLoginError

interface LoginUserUseCase {
    fun login(command: UserLoginCommand): DomainResult<LoginSessionInfo, UserLoginError>
}
