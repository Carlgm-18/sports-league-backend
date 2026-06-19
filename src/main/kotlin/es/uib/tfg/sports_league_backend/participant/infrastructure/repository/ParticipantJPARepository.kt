package es.uib.tfg.sports_league_backend.participant.infrastructure.repository

import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class ParticipantJPARepository (
    private val participantRepository: JpaParticipantRepository
): ParticipantRepository {
    override fun existsParticipant(userId: Long, leagueId: Long): Boolean =
        participantRepository.existsParticipantByUserIdAndLeagueId(userId, leagueId)


    override fun existsParticipant(participantId: Long): Boolean =
        participantRepository.existsParticipantById(participantId)


    override fun findParticipantById(participantId: Long): Participant? =
        participantRepository.findParticipantById(participantId)


    override fun findParticipant(userId: Long, leagueId: Long): Participant? =
        participantRepository.findParticipantByUserIdAndLeagueId(userId, leagueId)


    override fun findAllByLeagueId(leagueId: Long): List<Participant> =
        participantRepository.findAllByLeagueId(leagueId)

    override fun save(participant: Participant): Participant =
        participantRepository.save(participant)

}

interface JpaParticipantRepository : JpaRepository<Participant, Long> {
    fun existsParticipantByUserIdAndLeagueId(userId: Long, leagueId: Long): Boolean
    fun existsParticipantById(participantId: Long): Boolean
    fun findParticipantByUserIdAndLeagueId(userId: Long, leagueId: Long): Participant?
    fun findAllByLeagueId(leagueId: Long): List<Participant>
    fun findParticipantById(participantId: Long): Participant?
}
