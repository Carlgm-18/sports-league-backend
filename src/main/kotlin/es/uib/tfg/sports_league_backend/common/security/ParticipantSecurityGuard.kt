package es.uib.tfg.sports_league_backend.common.security

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.participant.application.ParticipantService
import org.springframework.stereotype.Component

@Component("participantSecurityGuard")
class ParticipantSecurityGuard(
    private val participantService: ParticipantService
) {
    fun canUpdateParticipant(userId: Long, participantId: Long): Boolean {
        val participant = participantService.findParticipantById(participantId)
            .let { (it as? DomainResult.Success)?.data } ?: return false
        return participant.user.id == userId
    }
}
