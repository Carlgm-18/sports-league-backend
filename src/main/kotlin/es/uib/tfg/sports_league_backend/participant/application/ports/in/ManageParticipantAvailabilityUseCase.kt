package es.uib.tfg.sports_league_backend.participant.application.ports.`in`

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantRetrieveError
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantUpdateError

interface ManageParticipantAvailabilityUseCase {
    fun findParticipantAvailability(userId: Long, leagueId: Long): DomainResult<List<DateTimeSlot>, ParticipantRetrieveError>
    fun updateParticipantAvailability(userId: Long, leagueId: Long, request: List<Long>): DomainResult<Unit, ParticipantUpdateError>
}
