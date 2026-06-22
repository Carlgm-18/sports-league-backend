package es.uib.tfg.sports_league_backend.participant.application

import es.uib.tfg.sports_league_backend.availability.application.AvailabilityService
import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueRepository
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantAvailability
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantRole
import es.uib.tfg.sports_league_backend.participant.domain.errors.AlreadyParticipant
import es.uib.tfg.sports_league_backend.participant.domain.errors.DorsalAlreadyTaken
import es.uib.tfg.sports_league_backend.participant.domain.errors.LeagueNotFound
import es.uib.tfg.sports_league_backend.participant.domain.errors.NotInATeam
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantJoinError
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantNotFound
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantRetrieveError
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantUpdateError
import es.uib.tfg.sports_league_backend.participant.domain.errors.UserNotFound
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipantJPARepository
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipationRoleRepository
import es.uib.tfg.sports_league_backend.user.application.UserService
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sportsapi.dto.ParticipantUpdateRequest
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ParticipantService(
    private val participantJPARepository: ParticipantJPARepository,
    private val participationRoleRepository: ParticipationRoleRepository,
    private val leagueRepository: LeagueRepository,
    private val userService: UserService,
    private val availabilityService: AvailabilityService
) {

    @Transactional
    fun registerOwner(user: User, league: League) {
        val participantOwner = Participant(user = user, league = league, roles = mutableSetOf())
        val adminRole = participationRoleRepository.findByRoleName("ADMIN")
        val participantRole = ParticipantRole(participant = participantOwner, participationRole = adminRole)
        participantOwner.roles.add(participantRole)

        participantJPARepository.save(participantOwner)
    }

    @Transactional
    fun registerPlayer(user: User, league: League): DomainResult<Participant, ParticipantJoinError> {

        // Validate that user has not already joined the league
        if (participantJPARepository.existsParticipant(league.id!!, user.id!!)) {
            return DomainResult.Failure(AlreadyParticipant)
        }

        val participantPlayer = Participant(user = user, league = league, roles = mutableSetOf())
        val playerRole = participationRoleRepository.findByRoleName("PLAYER")
        val participantRole = ParticipantRole(participant = participantPlayer, participationRole = playerRole)
        participantPlayer.roles.add(participantRole)

        return DomainResult.Success(participantJPARepository.save(participantPlayer))
    }

    fun findParticipantById(participantId: Long): DomainResult<Participant, ParticipantRetrieveError> =
        participantJPARepository.findParticipantById(participantId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(ParticipantNotFound)

    fun findParticipant(
        userId: Long,
        leagueId: Long
    ): DomainResult<Participant, ParticipantRetrieveError> {
        if(!leagueRepository.existsById(leagueId))
            return DomainResult.Failure(LeagueNotFound)

        if(userService.findUserById(userId) is DomainResult.Failure)
            return DomainResult.Failure(UserNotFound)

        return participantJPARepository.findParticipant(userId, leagueId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(UserNotFound)
    }

    fun save(participant: Participant): DomainResult<Participant, ParticipantJoinError> =
        participantJPARepository.save(participant).let { DomainResult.Success(it) }

    fun findAllLeagueParticipants(leagueId: Long): List<Participant> =
        participantJPARepository.findAllByLeagueId(leagueId)

    @Transactional
    fun updateParticipantById(
        participantId: Long,
        updateRequest: ParticipantUpdateRequest
    ): DomainResult<Participant, ParticipantUpdateError> {
        val participant = participantJPARepository.findParticipantById(participantId)
            ?: return DomainResult.Failure(ParticipantNotFound)

        if(participant.team == null)
            return DomainResult.Failure(NotInATeam)

        if(participant.team!!.members.map { it.dorsal }.contains(updateRequest.dorsal))
            return DomainResult.Failure(DorsalAlreadyTaken)

        participant.dorsal = updateRequest.dorsal
        return DomainResult.Success(participantJPARepository.save(participant))
    }

    fun isLeagueParticipantAndHasRole(userId: Long, leagueId: Long, role: String): Boolean =
        participantJPARepository.findParticipant(userId, leagueId)
            ?.roles?.map { it.participationRole.roleName }?.contains(role)
            ?: false

    fun findParticipantAvailability(
        userId: Long,
        leagueId: Long
    ): DomainResult<List<DateTimeSlot>, ParticipantRetrieveError> {
        if(!leagueRepository.existsById(leagueId))
            return DomainResult.Failure(LeagueNotFound)

        val participant = participantJPARepository.findParticipant(userId, leagueId)

        return DomainResult.Success(participant?.availability?.map { it.dateTimeSlot } ?: listOf())
    }

    @Transactional
    fun updateParticipantAvailability(
        userId: Long,
        leagueId: Long,
        request: List<Long>
    ): DomainResult<Unit, ParticipantUpdateError> {

        if(!leagueRepository.existsById(leagueId))
            return DomainResult.Failure(LeagueNotFound)

        val participant = participantJPARepository.findParticipant(userId, leagueId)
                            ?: return DomainResult.Failure(ParticipantNotFound)

        val newAvailabilities = request
                                    .map { availabilityService.findDateTimeSlotById(it) }
                                    .filter { it is DomainResult.Success }
                                    .map { ParticipantAvailability(
                                        participant = participant,
                                        dateTimeSlot = (it as DomainResult.Success).data
                                    ) }.toMutableList()

        participant.availability = newAvailabilities
        participantJPARepository.save(participant)
        return DomainResult.Success(Unit)
    }
}