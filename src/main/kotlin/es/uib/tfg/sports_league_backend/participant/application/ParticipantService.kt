package es.uib.tfg.sports_league_backend.participant.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantRole
import es.uib.tfg.sports_league_backend.participant.domain.errors.AlreadyParticipant
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantJoinError
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipantRepository
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipationRoleRepository
import es.uib.tfg.sports_league_backend.user.domain.User
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ParticipantService(
    private val participantRepository: ParticipantRepository,
    private val participationRoleRepository: ParticipationRoleRepository
) {

    @Transactional
    fun registerOwner(user: User, league: League) {
        val participantOwner = Participant(user = user, league = league, roles = mutableSetOf())
        val adminRole = participationRoleRepository.findByRoleName("ADMIN")
        val participantRole = ParticipantRole(participant = participantOwner, participationRole = adminRole)
        participantOwner.roles.add(participantRole)

        participantRepository.save(participantOwner)
    }

    @Transactional
    fun registerPlayer(user: User, league: League): DomainResult<Participant, ParticipantJoinError> {

        // Validate that user has not already joined the league
        if (participantRepository.existsParticipantByUserIdAndLeagueId(league.id!!, user.id!!)) {
            return DomainResult.Failure(AlreadyParticipant)
        }

        // Persist participant as PLAYER
        val participantPlayer = Participant(user = user, league = league, roles = mutableSetOf())
        val playerRole = participationRoleRepository.findByRoleName("PLAYER")
        val participantRole = ParticipantRole(participant = participantPlayer, participationRole = playerRole)
        participantPlayer.roles.add(participantRole)

        return DomainResult.Success(participantRepository.save(participantPlayer))
    }
}