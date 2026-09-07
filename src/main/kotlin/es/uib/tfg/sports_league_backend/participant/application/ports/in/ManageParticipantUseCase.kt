package es.uib.tfg.sports_league_backend.participant.application.ports.`in`

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantJoinError
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantRetrieveError
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantUpdateError
import es.uib.tfg.sportsapi.dto.ParticipantUpdateRequest

interface ManageParticipantUseCase {
    fun findParticipantById(participantId: Long): DomainResult<Participant, ParticipantRetrieveError>
    fun findParticipant(userId: Long, leagueId: Long): DomainResult<Participant, ParticipantRetrieveError>
    fun save(participant: Participant): DomainResult<Participant, ParticipantJoinError>
    fun findAllLeagueParticipants(leagueId: Long): List<Participant>
    fun updateParticipantById(participantId: Long, updateRequest: ParticipantUpdateRequest): DomainResult<Participant, ParticipantUpdateError>
    fun isLeagueParticipantAndHasRole(userId: Long, leagueId: Long, role: String): Boolean
    fun findLeaguesByUserId(userId: Long): List<es.uib.tfg.sports_league_backend.league.domain.League>
}
