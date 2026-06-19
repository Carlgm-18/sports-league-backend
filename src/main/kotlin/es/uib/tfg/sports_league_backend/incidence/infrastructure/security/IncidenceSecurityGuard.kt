package es.uib.tfg.sports_league_backend.incidence.infrastructure.security

import es.uib.tfg.sports_league_backend.incidence.infrastructure.repository.IncidenceRepository
import es.uib.tfg.sports_league_backend.participant.application.ParticipantService
import org.springframework.stereotype.Component
import org.springframework.data.repository.findByIdOrNull

@Component("incidenceSecurityGuard")
class IncidenceSecurityGuard(
    private val incidenceRepository: IncidenceRepository,
    private val participantService: ParticipantService
) {

    fun canManageIncidence(userId: Long, incidenceId: Long): Boolean {
        val incidence = incidenceRepository.findByIdOrNull(incidenceId) ?: return false

        if (incidence.creator.id == userId) return true

        return participantService.isLeagueParticipantAndHasRole(userId, incidence.leagueId, "ADMIN")
    }
}