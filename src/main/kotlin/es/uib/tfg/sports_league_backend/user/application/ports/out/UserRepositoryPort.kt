package es.uib.tfg.sports_league_backend.user.application.ports.out

import es.uib.tfg.sports_league_backend.user.domain.User

interface UserRepositoryPort {
    fun save(user: User): User
    fun findById(id: Long): User?
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}
