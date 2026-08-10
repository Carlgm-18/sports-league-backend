package es.uib.tfg.sports_league_backend.user.application.ports.`in`

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.application.update.UserUpdateCommand
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sports_league_backend.user.domain.errors.UserRetrieveError

interface UpdateUserUseCase {
    fun updateUserById(id: Long, newUser: UserUpdateCommand): DomainResult<User, UserRetrieveError>
}
