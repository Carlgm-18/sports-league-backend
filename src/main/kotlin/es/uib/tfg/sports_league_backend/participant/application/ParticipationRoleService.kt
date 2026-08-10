package es.uib.tfg.sports_league_backend.participant.application

import es.uib.tfg.sports_league_backend.participant.domain.ParticipationRole
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipationRoleJPAEntity
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipationRoleRepository
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.toDomain
import org.springframework.stereotype.Service

@Service
class ParticipationRoleService(
    private val participationRoleRepository: ParticipationRoleRepository,
) {
    fun findRoleByName(name: String): ParticipationRole =
        participationRoleRepository.findByRoleName(name).toDomain()
}