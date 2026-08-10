package es.uib.tfg.sports_league_backend.participant.infrastructure.repository

import es.uib.tfg.sports_league_backend.participant.application.ports.out.ParticipationRoleRepositoryPort
import es.uib.tfg.sports_league_backend.participant.domain.ParticipationRole
import org.springframework.stereotype.Component

@Component
class ParticipationRolePersistenceAdapter(
    private val participationRoleRepository: ParticipationRoleRepository
) : ParticipationRoleRepositoryPort {

    override fun findByRoleName(name: String): ParticipationRole {
        val entity = participationRoleRepository.findByRoleName(name)
            ?: throw NoSuchElementException("Role name $name not found")
        return entity.toDomain()
    }
}
