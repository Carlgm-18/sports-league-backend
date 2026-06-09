package es.uib.tfg.sports_league_backend.participant.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.application.LeagueService
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantRole
import es.uib.tfg.sports_league_backend.participant.domain.errors.AlreadyParticipant
import es.uib.tfg.sports_league_backend.participant.domain.errors.DorsalAlreadyTaken
import es.uib.tfg.sports_league_backend.participant.domain.errors.LeagueNotFound
import es.uib.tfg.sports_league_backend.participant.domain.errors.NotInATeam
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantJoinError
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantNotFound
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantRetrieveError
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantUpdateError
import es.uib.tfg.sports_league_backend.participant.domain.errors.UnauthorizedAction
import es.uib.tfg.sports_league_backend.participant.domain.errors.UserNotFound
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipantRepository
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipationRoleRepository
import es.uib.tfg.sports_league_backend.user.application.UserService
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sportsapi.dto.ParticipantUpdateRequest
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ParticipantService(
    private val participantRepository: ParticipantRepository,
    private val participationRoleRepository: ParticipationRoleRepository,
    private val leagueService: LeagueService,
    private val userService: UserService
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

        val participantPlayer = Participant(user = user, league = league, roles = mutableSetOf())
        val playerRole = participationRoleRepository.findByRoleName("PLAYER")
        val participantRole = ParticipantRole(participant = participantPlayer, participationRole = playerRole)
        participantPlayer.roles.add(participantRole)

        return DomainResult.Success(participantRepository.save(participantPlayer))
    }

    fun findParticipantById(participantId: Long): DomainResult<Participant, ParticipantRetrieveError> =
        participantRepository.findByIdOrNull(participantId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(ParticipantNotFound)

    fun findParticipantByUserIdAndLeagueId(
        userId: Long,
        leagueId: Long
    ): DomainResult<Participant, ParticipantRetrieveError> {
        if(leagueService.findLeagueById(leagueId) is DomainResult.Failure)
            return DomainResult.Failure(LeagueNotFound)

        if(userService.findUserById(userId) is DomainResult.Failure)
            return DomainResult.Failure(UserNotFound)

        return participantRepository.findByUserIdAndLeagueId(userId, leagueId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(UserNotFound)
    }

    fun save(participant: Participant): DomainResult<Participant, ParticipantJoinError> =
        participantRepository.save(participant).let { DomainResult.Success(it) }

    fun findAllLeagueParticipants(leagueId: Long): List<Participant> =
        participantRepository.findAllByLeagueId(leagueId)

}