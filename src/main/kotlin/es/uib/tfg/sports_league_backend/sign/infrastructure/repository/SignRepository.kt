package es.uib.tfg.sports_league_backend.sign.infrastructure.repository

import es.uib.tfg.sports_league_backend.sign.domain.Sign
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SignRepository : JpaRepository<Sign, Long> {
    fun findFirstByUserIdOrderByUploadAtDesc(userId: Long): Sign?
}