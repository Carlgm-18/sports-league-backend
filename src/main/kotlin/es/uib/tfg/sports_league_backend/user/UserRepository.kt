package es.uib.tfg.sports_league_backend.user

import es.uib.tfg.sports_league_backend.user.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Int> {

    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): User?
}