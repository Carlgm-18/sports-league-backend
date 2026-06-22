package es.uib.tfg.sports_league_backend.match.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.match.domain.Match
import es.uib.tfg.sports_league_backend.match.domain.Proposal
import es.uib.tfg.sports_league_backend.match.infrastructure.repository.MatchRepository
import es.uib.tfg.sports_league_backend.match.infrastructure.repository.ProposalRepository
import es.uib.tfg.sports_league_backend.phase.application.TournamentSlotService
import es.uib.tfg.sports_league_backend.round.application.RoundService
import es.uib.tfg.sports_league_backend.participant.application.ParticipantService
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sportsapi.dto.MatchState
import es.uib.tfg.sportsapi.dto.ProposalState
import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import es.uib.tfg.sports_league_backend.match.domain.errors.AssignRefereeError
import es.uib.tfg.sports_league_backend.match.domain.errors.IncompatibleMatchAndReferee
import es.uib.tfg.sports_league_backend.match.domain.errors.ResolveOwnProposalError
import es.uib.tfg.sports_league_backend.match.domain.errors.InexistentAvailabilityForThisRound
import es.uib.tfg.sports_league_backend.match.domain.errors.InvalidState
import es.uib.tfg.sports_league_backend.match.domain.errors.LeagueNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchAlreadyEnded
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchNotScheduledYet
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchRetrieveError
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchUpdateError
import es.uib.tfg.sports_league_backend.match.domain.errors.NoAvailableReferees
import es.uib.tfg.sports_league_backend.match.domain.errors.NotRefereeParticipant
import es.uib.tfg.sports_league_backend.match.domain.errors.ParticipantNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.ProposalAlreadyResolved
import es.uib.tfg.sports_league_backend.match.domain.errors.ProposalCreateError
import es.uib.tfg.sports_league_backend.match.domain.errors.ProposalNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.ProposalResolveError
import es.uib.tfg.sports_league_backend.match.domain.errors.RefereeNotAssigned
import es.uib.tfg.sports_league_backend.match.domain.errors.ScheduleAlreadyTaken
import es.uib.tfg.sports_league_backend.match.domain.errors.TeamNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.TeamNotInMatch
import es.uib.tfg.sports_league_backend.match.domain.errors.UserNotFound
import es.uib.tfg.sports_league_backend.match.infrastructure.controller.RefereeType
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.team.application.TeamService
import es.uib.tfg.sportsapi.dto.MatchDateProposalResolveRequest
import es.uib.tfg.sportsapi.dto.MatchUpdateRequest
import es.uib.tfg.sports_league_backend.participant.domain.errors.LeagueNotFound as ParticipantLeagueNotFound
import es.uib.tfg.sports_league_backend.participant.domain.errors.UserNotFound as ParticipantUserNotFound
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantNotFound as ParticipantParticipantNotFound
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val tournamentSlotService: TournamentSlotService,
    private val proposalRepository: ProposalRepository,
    private val roundService: RoundService,
    private val participantService: ParticipantService,
    private val teamService: TeamService
) {
    @Transactional
    fun save(match: Match): Match =
        matchRepository.save(match)

    @Transactional
    fun saveAll(matches: List<Match>): List<Match> =
        matchRepository.saveAll(matches)

    fun finMatchById(matchId: Long): DomainResult<Match, MatchRetrieveError> =
        matchRepository.findByIdOrNull(matchId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(MatchNotFound)

    @Transactional
    fun updateMatch(matchId: Long, request: MatchUpdateRequest): DomainResult<Match, MatchUpdateError> {
        val match = matchRepository.findByIdOrNull(matchId)
                        ?: return DomainResult.Failure(MatchNotFound)

        return when(request.status) {
            MatchState.IN_GAME ->
                startMatch(match)

            MatchState.ENDED -> {
                endMatch(match,
                    request.result?.winnerTeamId
                    ?: return DomainResult.Failure(InvalidState)
                )
            }
            else -> DomainResult.Failure(InvalidState)
        }
    }

    @Transactional
    fun startMatch(match: Match): DomainResult<Match, MatchUpdateError> {
        if(match.firstReferee == null)
            return DomainResult.Failure(RefereeNotAssigned)

        match.status = MatchState.IN_GAME

        return DomainResult.Success(matchRepository.save(match))
    }

    @Transactional
    fun endMatch(match: Match, winnerId: Long): DomainResult<Match, MatchUpdateError> {

        if (match.status == MatchState.ENDED)
            return DomainResult.Failure(MatchAlreadyEnded)

        val winner = when(val result = teamService.findById(winnerId)) {
            is DomainResult.Success -> result.data
            is DomainResult.Failure -> return DomainResult.Failure(TeamNotFound)
        }


        if (match.localTeam?.id != winnerId && match.visitorTeam?.id != winnerId)
            return DomainResult.Failure(TeamNotInMatch)

        // Change match status to ENDED
        match.status = MatchState.ENDED
        val savedMatch = matchRepository.save(match)
        
        // Propagate winner if it's a tournament match
        propagateTournamentMatch(match.id!!, winner)

        return DomainResult.Success(savedMatch)
    }

    private fun propagateTournamentMatch(matchId: Long, winner: Team) {
        val slot = tournamentSlotService.findByMatchId(matchId)
        if (slot != null) {
            val phase = slot.phase
            val stages = phase.stagesNumber
            val k = slot.indexOrder

            // Calculate target match index order
            val targetIndex = getTargetMatchIndex(k, stages)
            if (targetIndex != null) {
                val targetSlot = tournamentSlotService.findByPhaseIdAndIndexOrder(phase.id!!, targetIndex)
                if (targetSlot != null) {
                    val targetMatch = targetSlot.match

                    // Determine if this match is even or odd relative index in its stage
                    val start = getStageStartIndex(getStageOfMatch(k, stages), stages)
                    val rel = k - start

                    if (rel % 2 == 0) {
                        targetMatch.localTeam = winner
                    } else {
                        targetMatch.visitorTeam = winner
                    }

                    matchRepository.save(targetMatch)
                }
            }
        }
    }

    private fun getStageOfMatch(k: Int, stages: Int): Int {
        var start = 1
        for (s in 1..stages) {
            val count = 1 shl (stages - s)
            if (k in start until (start + count)) {
                return s
            }
            start += count
        }
        return stages
    }

    private fun getStageStartIndex(s: Int, stages: Int): Int {
        var start = 1
        for (i in 1 until s) {
            start += 1 shl (stages - i)
        }
        return start
    }

    private fun getTargetMatchIndex(k: Int, stages: Int): Int? {
        val s = getStageOfMatch(k, stages)
        if (s == stages) return null // Final level has no target
        
        val start = getStageStartIndex(s, stages)
        val nextStart = getStageStartIndex(s + 1, stages)
        val rel = k - start
        val targetRel = rel / 2
        return nextStart + targetRel
    }

    fun getActiveProposal(matchId: Long): Proposal? {
        return proposalRepository.findFirstByMatchIdAndStatusOrderByProposedAtDesc(matchId, ProposalState.PENDING)
            ?: proposalRepository.findFirstByMatchIdAndStatusOrderByProposedAtDesc(matchId, ProposalState.APPROVED)
    }

    private fun getParticipant(userId: Long, leagueId: Long): DomainResult<Participant, ProposalResolveError> =
        when(val result = participantService.findParticipant(userId, leagueId)) {
            is DomainResult.Success -> result

            is DomainResult.Failure ->
                when(result.error) {
                    ParticipantParticipantNotFound ->
                        DomainResult.Failure(ParticipantNotFound)

                    ParticipantLeagueNotFound ->
                        DomainResult.Failure(LeagueNotFound)

                    ParticipantUserNotFound ->
                        DomainResult.Failure(UserNotFound)
                }
        }


    @Transactional
    fun createProposal(
        matchId: Long,
        userId: Long,
        proposedSlotId: Long
    ): DomainResult<Proposal, ProposalCreateError> {
        val match = matchRepository.findByIdOrNull(matchId)
            ?: return DomainResult.Failure(MatchNotFound)

        val round = (roundService.findRoundById(match.roundId) as DomainResult.Success).data

        val matchingSlot = round.availability.find { it.id == proposedSlotId }
            ?: return DomainResult.Failure(InexistentAvailabilityForThisRound)

        val league = round.phase.league

        val participantTeam = when(val result = getParticipant(userId, league.id!!)) {
            is DomainResult.Success -> result.data.team
            is DomainResult.Failure -> return DomainResult.Failure(result.error as ProposalCreateError)
        }

        val matches = matchRepository.findAllByLeagueId(league.id!!)

        if(matches.any {it.dateTime?.id == proposedSlotId})
            return DomainResult.Failure(ScheduleAlreadyTaken)

        val newProposal = Proposal(
            match = match,
            dateTimeSlot = matchingSlot,
            team = participantTeam!!,
            status = ProposalState.PENDING,
            proposedAt = LocalDateTime.now()
        )

        val savedProposal = proposalRepository.save(newProposal)
        return DomainResult.Success(savedProposal)
    }

    @Transactional
    fun resolveProposal(
        proposalId: Long,
        userId: Long,
        resolveState: MatchDateProposalResolveRequest
    ): DomainResult<Proposal, ProposalResolveError>  {
        val proposal = proposalRepository.findByIdOrNull(proposalId)
            ?: return DomainResult.Failure(ProposalNotFound)

        if(proposal.status != ProposalState.PENDING) {
            return DomainResult.Failure(ProposalAlreadyResolved)
        }

        val participant = when(val result = getParticipant(userId, proposal.team.league.id!!)) {
            is DomainResult.Success -> result.data
            is DomainResult.Failure -> return result
        }

        if (proposal.team.id == participant.team?.id) {
            return DomainResult.Failure(ResolveOwnProposalError)
        }

        val match = proposal.match

        proposal.status = resolveState.status
        proposal.resolvedAt = LocalDateTime.now()
        val resolvedProposal = proposalRepository.save(proposal)

        if(resolvedProposal.status == ProposalState.APPROVED) {
            match.dateTime = proposal.dateTimeSlot
            match.status = MatchState.SCHEDULED
            matchRepository.save(match)
        }

        return DomainResult.Success(resolvedProposal)
    }

    private fun DateTimeSlot.overlapsWith(other: DateTimeSlot): Boolean {
        val start1 = this.dateTime
        val end1 = this.dateTime.plusHours(this.duration.toLong())
        val start2 = other.dateTime
        val end2 = other.dateTime.plusHours(other.duration.toLong())
        return start1.isBefore(end2) && start2.isBefore(end1)
    }

    @Transactional
    fun autoAssignReferee(matchId: Long): DomainResult<Match, AssignRefereeError> {
        val match = matchRepository.findByIdOrNull(matchId)
            ?: return DomainResult.Failure(MatchNotFound)

        val targetSlot = match.dateTime
            ?: return DomainResult.Failure(MatchNotScheduledYet)

        val round = (roundService.findRoundById(match.roundId) as DomainResult.Success).data

        val leagueId = round.phase.league.id!!
        val referees = participantService.findAllLeagueParticipants(leagueId).filter { participant ->
            participant.roles.any { it.participationRole.roleName == "REFEREE" }
        }

        if (referees.isEmpty()) {
            return DomainResult.Failure(NoAvailableReferees)
        }

        val leagueMatches = matchRepository.findAllByLeagueId(leagueId)

        val availableReferees = referees.filter { referee ->
            val refereeMatches = leagueMatches.filter { m ->
                (m.firstReferee?.id == referee.id || m.secondReferee?.id == referee.id)
            }
            refereeMatches.none { m -> m.dateTime!!.overlapsWith(targetSlot) }
        }

        if (availableReferees.isEmpty()) {
            return DomainResult.Failure(NoAvailableReferees)
        }

        val refereeCounts = availableReferees.associateWith { referee ->
            leagueMatches.count { m ->
                m.firstReferee?.id == referee.id || m.secondReferee?.id == referee.id
            }
        }

        val chosenReferee = refereeCounts.minByOrNull { it.value }!!.key

        match.firstReferee = chosenReferee
        val savedMatch = matchRepository.save(match)
        return DomainResult.Success(savedMatch)
    }

    @Transactional
    fun forceAssignReferee(
            matchId: Long,
            refereeParticipantId: Long,
            refereeType: RefereeType
        ): DomainResult<Match, AssignRefereeError> {
        val match = matchRepository.findByIdOrNull(matchId)
            ?: return DomainResult.Failure(MatchNotFound)

        val round = (roundService.findRoundById(match.roundId) as DomainResult.Success).data

        val leagueId = round.phase.league.id!!

        val referee = when(val result = participantService.findParticipantById(refereeParticipantId)) {
            is DomainResult.Success -> result.data
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
        }

        if (referee.league.id != leagueId) {
            return DomainResult.Failure(IncompatibleMatchAndReferee)
        }

        val isReferee = referee.roles.any { it.participationRole.roleName == "REFEREE" }
        if (!isReferee) {
            return DomainResult.Failure(NotRefereeParticipant)
        }

        when(refereeType) {
            RefereeType.FIRST -> match.firstReferee = referee
            RefereeType.SECOND -> match.secondReferee = referee
        }

        val savedMatch = matchRepository.save(match)
        return DomainResult.Success(savedMatch)
    }
}