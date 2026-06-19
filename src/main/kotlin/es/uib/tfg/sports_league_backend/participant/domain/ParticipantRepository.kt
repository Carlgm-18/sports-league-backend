package es.uib.tfg.sports_league_backend.participant.domain

interface ParticipantRepository {
    fun existsParticipant(userId: Long, leagueId: Long): Boolean
    fun existsParticipant(participantId: Long): Boolean
    fun findParticipantById(participantId: Long): Participant?
    fun findParticipant(userId: Long, leagueId: Long): Participant?
    fun findAllByLeagueId(leagueId: Long): List<Participant>
    fun save(participant: Participant): Participant
}