package es.uib.tfg.sports_league_backend.participant.application.ports.out

import es.uib.tfg.sports_league_backend.participant.domain.ParticipationRole

interface ParticipationRoleRepositoryPort {
    fun findByRoleName(name: String): ParticipationRole
}
