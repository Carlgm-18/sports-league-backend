package es.uib.tfg.sports_league_backend.league.infrastructure.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LeagueRepository : JpaRepository<LeagueJPAEntity, Long> {
    fun findAllByDeletedAtIsNull(): List<LeagueJPAEntity>
    fun findByIdAndDeletedAtIsNull(id: Long): LeagueJPAEntity?
    fun existsByIdAndDeletedAtIsNull(id: Long): Boolean
}