package es.uib.tfg.sports_league_backend.participant.infrastructure.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParticipationRoleRepository: JpaRepository<ParticipationRoleJPAEntity, Long> {
    fun findByRoleName(name: String): ParticipationRoleJPAEntity
}