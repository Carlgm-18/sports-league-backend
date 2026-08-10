package es.uib.tfg.sports_league_backend.common.security

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.ManageParticipantUseCase
import org.springframework.stereotype.Component

@Component("participantSecurityGuard")
class ParticipantSecurityGuard(
    private val manageParticipantUseCase: ManageParticipantUseCase
) {
    fun canUpdateParticipant(userId: Long, participantId: Long): Boolean {
        val participant = manageParticipantUseCase.findParticipantById(participantId)
            .let { (it as? DomainResult.Success)?.data } ?: return false
        return participant.user.id == userId
    }
}
