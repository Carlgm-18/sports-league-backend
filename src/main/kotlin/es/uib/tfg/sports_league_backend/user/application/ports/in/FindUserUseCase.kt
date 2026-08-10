package es.uib.tfg.sports_league_backend.user.application.ports.`in`

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sports_league_backend.user.domain.errors.UserRetrieveError

interface FindUserUseCase {
    fun findUserById(id: Long): DomainResult<User, UserRetrieveError>
}
