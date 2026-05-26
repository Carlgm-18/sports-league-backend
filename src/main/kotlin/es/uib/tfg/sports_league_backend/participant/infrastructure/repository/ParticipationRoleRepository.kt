package es.uib.tfg.sports_league_backend.participant.infrastructure.repository

import es.uib.tfg.sports_league_backend.participant.domain.ParticipationRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParticipationRoleRepository: JpaRepository<ParticipationRole, Long> {
    fun findByRoleName(name: String): ParticipationRole
}