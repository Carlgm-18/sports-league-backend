package es.uib.tfg.sports_league_backend.participant.infrastructure.repository

import es.uib.tfg.sports_league_backend.participant.domain.Participant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParticipantRepository: JpaRepository<Participant, Long> {
    fun existsParticipantByUserIdAndLeagueId(userId: Long, leagueId: Long): Boolean
    fun findByUserIdAndLeagueId(userId: Long, leagueId: Long): Participant?
    fun findAllByLeagueId(leagueId: Long): List<Participant>
}