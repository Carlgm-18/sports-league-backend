package es.uib.tfg.sports_league_backend.participant.application

import es.uib.tfg.sports_league_backend.availability.application.AvailabilityService
import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueRepository
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.RegisterParticipantUseCase
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.ManageParticipantUseCase
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.ManageParticipantAvailabilityUseCase
import es.uib.tfg.sports_league_backend.participant.application.ports.out.ParticipationRoleRepositoryPort
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantAvailability
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantRole
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantRepository
import es.uib.tfg.sports_league_backend.participant.domain.errors.*
import es.uib.tfg.sports_league_backend.user.application.ports.`in`.FindUserUseCase
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sportsapi.dto.ParticipantUpdateRequest
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ParticipantService(
    private val participantJPARepository: ParticipantRepository,
    private val participationRoleRepository: ParticipationRoleRepositoryPort,
    private val leagueRepository: LeagueRepository,
    private val findUserUseCase: FindUserUseCase,
    private val availabilityService: AvailabilityService
) : RegisterParticipantUseCase, ManageParticipantUseCase, ManageParticipantAvailabilityUseCase {

    @Transactional
    override fun registerOwner(user: User, league: League) {
        val participantOwner = Participant(user = user, league = league, roles = mutableSetOf())
        val adminRole = participationRoleRepository.findByRoleName("ADMIN")
        val participantRole = ParticipantRole(participant = participantOwner, participationRole = adminRole)
        participantOwner.roles.add(participantRole)

        participantJPARepository.save(participantOwner)
    }

    @Transactional
    override fun registerPlayer(user: User, league: League): DomainResult<Participant, ParticipantJoinError> {

        if (participantJPARepository.existsParticipant(league.id!!, user.id!!)) {
            return DomainResult.Failure(AlreadyParticipant)
        }

        val participantPlayer = Participant(user = user, league = league, roles = mutableSetOf())
        val playerRole = participationRoleRepository.findByRoleName("PLAYER")
        val participantRole = ParticipantRole(participant = participantPlayer, participationRole = playerRole)
        participantPlayer.roles.add(participantRole)

        return DomainResult.Success(participantJPARepository.save(participantPlayer))
    }

    override fun findParticipantById(participantId: Long): DomainResult<Participant, ParticipantRetrieveError> =
        participantJPARepository.findParticipantById(participantId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(ParticipantNotFound)

    override fun findParticipant(
        userId: Long,
        leagueId: Long
    ): DomainResult<Participant, ParticipantRetrieveError> {
        if(!leagueRepository.existsById(leagueId))
            return DomainResult.Failure(LeagueNotFound)

        if(findUserUseCase.findUserById(userId) is DomainResult.Failure)
            return DomainResult.Failure(UserNotFound)

        return participantJPARepository.findParticipant(userId, leagueId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(UserNotFound)
    }

    override fun save(participant: Participant): DomainResult<Participant, ParticipantJoinError> =
        participantJPARepository.save(participant).let { DomainResult.Success(it) }

    override fun findAllLeagueParticipants(leagueId: Long): List<Participant> =
        participantJPARepository.findAllByLeagueId(leagueId)

    @Transactional
    override fun updateParticipantById(
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

    override fun isLeagueParticipantAndHasRole(userId: Long, leagueId: Long, role: String): Boolean =
        participantJPARepository.findParticipant(userId, leagueId)
            ?.roles?.map { it.participationRole.roleName }?.contains(role)
            ?: false

    override fun findParticipantAvailability(
        userId: Long,
        leagueId: Long
    ): DomainResult<List<DateTimeSlot>, ParticipantRetrieveError> {
        if(!leagueRepository.existsById(leagueId))
            return DomainResult.Failure(LeagueNotFound)

        val participant = participantJPARepository.findParticipant(userId, leagueId)

        return DomainResult.Success(participant?.availability?.map { it.dateTimeSlot } ?: listOf())
    }

    @Transactional
    override fun updateParticipantAvailability(
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

    override fun findLeaguesByUserId(userId: Long): List<League> {
        val participants = participantJPARepository.findAllByUserId(userId)
        return participants.map { it.league }
    }

    @Transactional
    fun removeMemberFromTeam(captainUserId: Long, teamId: Long, participantId: Long): DomainResult<Unit, ParticipantUpdateError> {
        val participantToKick = participantJPARepository.findParticipantById(participantId)
            ?: return DomainResult.Failure(ParticipantNotFound)
        
        if (participantToKick.team?.id != teamId) {
            return DomainResult.Failure(NotInATeam)
        }
        
        val leagueId = participantToKick.league.id!!
        val captain = participantJPARepository.findParticipant(captainUserId, leagueId)
            ?: return DomainResult.Failure(UnauthorizedAction)
            
        val isCaptain = captain.team?.id == teamId && captain.roles.any { it.participationRole.roleName == "CAPTAIN" }
        val isAdmin = captain.roles.any { it.participationRole.roleName == "ADMIN" }
        
        if (!isCaptain && !isAdmin) {
            return DomainResult.Failure(UnauthorizedAction)
        }
        
        participantToKick.team = null
        participantToKick.dorsal = null
        participantToKick.roles.removeIf { it.participationRole.roleName == "CAPTAIN" }
        
        participantJPARepository.save(participantToKick)
        return DomainResult.Success(Unit)
    }
}