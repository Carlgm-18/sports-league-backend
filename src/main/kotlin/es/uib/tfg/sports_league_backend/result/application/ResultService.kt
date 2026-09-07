package es.uib.tfg.sports_league_backend.result.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.match.domain.Match
import es.uib.tfg.sports_league_backend.match.infrastructure.repository.MatchRepository
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchUpdateError
import es.uib.tfg.sports_league_backend.result.domain.Result
import es.uib.tfg.sports_league_backend.result.domain.Observation
import es.uib.tfg.sports_league_backend.result.domain.Lineup
import es.uib.tfg.sports_league_backend.result.domain.MatchPeriod
import es.uib.tfg.sports_league_backend.result.domain.MatchPeriodType
import es.uib.tfg.sports_league_backend.result.domain.match_event.MatchEvent
import es.uib.tfg.sports_league_backend.result.domain.match_event.Timeout
import es.uib.tfg.sports_league_backend.result.domain.match_event.Substitution
import es.uib.tfg.sports_league_backend.result.domain.match_event.Sanction
import es.uib.tfg.sports_league_backend.result.domain.match_event.SanctionType
import es.uib.tfg.sports_league_backend.result.infrastructure.repository.*
import es.uib.tfg.sportsapi.dto.ResultDetails
import es.uib.tfg.sportsapi.dto.MatchState
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.sql.Time
import java.time.LocalTime

@Service
class ResultService(
    private val resultRepository: ResultRepository,
    private val matchRepository: MatchRepository,
    private val matchPeriodTypeRepository: MatchPeriodTypeRepository,
    private val sanctionTypeRepository: SanctionTypeRepository,
    private val lineupRepository: LineupRepository
) {

    @Transactional
    fun saveResult(matchId: Long, dto: ResultDetails): DomainResult<Result, MatchUpdateError> {
        val match = matchRepository.findByIdOrNull(matchId)
            ?: return DomainResult.Failure(MatchNotFound)

        var result = resultRepository.findByIdOrNull(matchId)
        if (result == null) {
            result = Result(
                id = matchId,
                match = match,
                localTotalScore = dto.localTotalScore,
                visitorTotalScore = dto.visitorTotalScore,
                recordUrl = dto.recordUrl
            )
            result = resultRepository.saveAndFlush(result)
        } else {
            result.localTotalScore = dto.localTotalScore
            result.visitorTotalScore = dto.visitorTotalScore
            result.recordUrl = dto.recordUrl
            result.observations.clear()
            result.matchPeriod.clear()
            result.lineups.clear()
            resultRepository.saveAndFlush(result)
        }

        val newLineups = mutableListOf<Lineup>()
        val localMembers = match.localTeam?.members ?: emptyList()
        val visitorMembers = match.visitorTeam?.members ?: emptyList()
        for (p in (localMembers + visitorMembers)) {
            newLineups.add(Lineup(
                result = result,
                team = p.team!!,
                participant = p,
                matchDorsal = p.dorsal
            ))
        }
        result.lineups.addAll(newLineups)
        resultRepository.saveAndFlush(result)

        val newObs = dto.observations.map { text ->
            Observation(text = text, result = result)
        }
        result.observations.addAll(newObs)

        for (dtoPeriod in dto.periods) {
            val periodType = matchPeriodTypeRepository.findByPeriodTypeName(dtoPeriod.periodType)
                ?: matchPeriodTypeRepository.save(MatchPeriodType(periodTypeName = dtoPeriod.periodType))

            val period = MatchPeriod(
                periodType = periodType,
                periodNumber = dtoPeriod.periodNumber,
                localScore = dtoPeriod.localScore,
                visitorScore = dtoPeriod.visitorScore,
                result = result
            )

            val events = mutableListOf<MatchEvent>()
            for (dtoEvent in dtoPeriod.events) {
                val time = Time.valueOf(LocalTime.ofSecondOfDay(dtoEvent.happenedAtTime.toLong()))
                val triggerTeam = if (dtoEvent.responsibleTeamId != 0L) {
                    if (match.localTeam?.id == dtoEvent.responsibleTeamId) match.localTeam
                    else if (match.visitorTeam?.id == dtoEvent.responsibleTeamId) match.visitorTeam
                    else null
                } else null

                if (dtoEvent.durationTime.isNotBlank() && dtoEvent.durationTime != "null" && dtoEvent.durationTime != "00:00") {
                    val parts = dtoEvent.durationTime.split(":")
                    val min = parts[0].toInt()
                    val sec = parts[1].toInt()
                    val duration = Time.valueOf(LocalTime.of(0, min, sec))

                    events.add(Timeout(
                        happenedAtTime = time,
                        atLocalScore = dtoEvent.atLocalScore,
                        atVisitorScore = dtoEvent.atVisitorScore,
                        period = period,
                        triggerTeam = triggerTeam,
                        durationTime = duration
                    ))
                } else if (dtoEvent.sactionType.isNotBlank() && dtoEvent.sactionType != "null") {
                    val appliedToLineup = result.lineups.firstOrNull { it.participant.id == dtoEvent.appliedTo.participantId }
                        ?: throw IllegalArgumentException("Participant ${dtoEvent.appliedTo.participantId} not in lineup")

                    val sanctionType = sanctionTypeRepository.findBySanctionTypeName(dtoEvent.sactionType)
                        ?: sanctionTypeRepository.save(SanctionType(sanctionTypeName = dtoEvent.sactionType))

                    events.add(Sanction(
                        happenedAtTime = time,
                        atLocalScore = dtoEvent.atLocalScore,
                        atVisitorScore = dtoEvent.atVisitorScore,
                        period = period,
                        triggerTeam = triggerTeam,
                        appliedTo = appliedToLineup,
                        sanctionType = sanctionType,
                        reason = dtoEvent.reason
                    ))
                } else {
                    val incomingLineup = result.lineups.firstOrNull { it.participant.id == dtoEvent.incomingPlayer.participantId }
                        ?: throw IllegalArgumentException("Incoming Participant ${dtoEvent.incomingPlayer.participantId} not in lineup")
                    val outgoingLineup = result.lineups.firstOrNull { it.participant.id == dtoEvent.outgoingPlayer.participantId }
                        ?: throw IllegalArgumentException("Outgoing Participant ${dtoEvent.outgoingPlayer.participantId} not in lineup")

                    events.add(Substitution(
                        happenedAtTime = time,
                        atLocalScore = dtoEvent.atLocalScore,
                        atVisitorScore = dtoEvent.atVisitorScore,
                        period = period,
                        triggerTeam = triggerTeam,
                        incomingPlayer = incomingLineup,
                        outgoingPlayer = outgoingLineup
                    ))
                }
            }
            period.matchEvents.addAll(events)
            result.matchPeriod.add(period)
        }

        match.status = MatchState.ENDED
        matchRepository.save(match)

        val savedResult = resultRepository.save(result)
        return DomainResult.Success(savedResult)
    }

    @Transactional(readOnly = true)
    fun getResult(matchId: Long): DomainResult<Result, MatchUpdateError> {
        val result = resultRepository.findByIdOrNull(matchId)
            ?: return DomainResult.Failure(MatchNotFound)
        return DomainResult.Success(result)
    }
}
