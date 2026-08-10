package es.uib.tfg.sports_league_backend.user.infrastructure.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserJPAEntity, Long> {

    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): UserJPAEntity?
}