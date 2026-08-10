package es.uib.tfg.sports_league_backend.result.infrastructure.mapper

import es.uib.tfg.sports_league_backend.result.domain.Result
import es.uib.tfg.sports_league_backend.result.domain.match_sign.MatchSign
import es.uib.tfg.sports_league_backend.result.domain.match_sign.Moment
import es.uib.tfg.sports_league_backend.result.domain.match_sign.InMatchRole
import es.uib.tfg.sports_league_backend.result.domain.match_event.MatchEvent
import es.uib.tfg.sports_league_backend.result.domain.match_event.Timeout
import es.uib.tfg.sports_league_backend.result.domain.match_event.Substitution
import es.uib.tfg.sports_league_backend.result.domain.match_event.Sanction
import es.uib.tfg.sports_league_backend.participant.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.toDomain
import es.uib.tfg.sportsapi.dto.*
import java.net.URI
import java.time.LocalDateTime

fun Result.toSummaryDTO() = ResultSummary(
    localTotalScore = localTotalScore,
    visitorTotalScore = visitorTotalScore,
    recordUrl = recordUrl ?: URI.create("")
)

fun Result.toDetailsDTO(): ResultDetails {
    val obsList = observations.map { it.text }
    
    val periodList = matchPeriod.map { p ->
        MatchPeriod(
            periodNumber = p.periodNumber,
            localScore = p.localScore,
            visitorScore = p.visitorScore,
            periodType = p.periodType.periodTypeName,
            events = p.matchEvents.map { it.toDTO() }
        )
    }
    
    val beforeSigns = matchSigns.filter { it.moment == Moment.PRE_MATCH }
    val afterSigns = matchSigns.filter { it.moment == Moment.POST_MATCH }
    
    fun getSignatureDTO(signs: List<MatchSign>, role: InMatchRole): SignImageUrl {
        val sign = signs.firstOrNull { it.inMatchRole == role }?.sign
        return SignImageUrl(
            signImageUrl = sign?.signImageUrl?.let { URI.create(it) },
            uploatAt = sign?.uploadAt
        )
    }
    
    val beforeDTO = MatchSignatures(
        firstRefereeSignature = getSignatureDTO(beforeSigns, InMatchRole.FIRST_REFEREE),
        secondRefereeSignature = getSignatureDTO(beforeSigns, InMatchRole.SECOND_REFEREE),
        localCaptainSignature = getSignatureDTO(beforeSigns, InMatchRole.LOCAL_CAPTAIN),
        visitorCaptainSignature = getSignatureDTO(beforeSigns, InMatchRole.VISITOR_CAPTAIN)
    )
    
    val afterDTO = MatchSignatures(
        firstRefereeSignature = getSignatureDTO(afterSigns, InMatchRole.FIRST_REFEREE),
        secondRefereeSignature = getSignatureDTO(afterSigns, InMatchRole.SECOND_REFEREE),
        localCaptainSignature = getSignatureDTO(afterSigns, InMatchRole.LOCAL_CAPTAIN),
        visitorCaptainSignature = getSignatureDTO(afterSigns, InMatchRole.VISITOR_CAPTAIN)
    )
    
    val signaturesDTO = ResultDetailsAllOfSignatures(
        beforeMatchSignatures = beforeDTO,
        afterMatchSignatures = afterDTO
    )
    
    return ResultDetails(
        localTotalScore = localTotalScore,
        visitorTotalScore = visitorTotalScore,
        recordUrl = recordUrl ?: URI.create(""),
        signatures = signaturesDTO,
        observations = obsList,
        periods = periodList
    )
}

fun MatchEvent.toDTO(): MatchPeriodEventsInner {
    val secondsOfDay = happenedAtTime.toLocalTime().toSecondOfDay()
    val respTeamId = triggerTeam?.id ?: 0L
    
    val dummyParticipant = ParticipantDetails(
        participantId = 0L,
        userId = 0L,
        leagueId = 0L,
        roles = emptyList(),
        joinDate = LocalDateTime.now()
    )
    
    return when (this) {
        is Timeout -> {
            val localTime = durationTime.toLocalTime()
            val durStr = String.format("%02d:%02d", localTime.minute, localTime.second)
            MatchPeriodEventsInner(
                happenedAtTime = secondsOfDay,
                atLocalScore = atLocalScore,
                atVisitorScore = atVisitorScore,
                responsibleTeamId = respTeamId,
                incomingPlayer = dummyParticipant,
                outgoingPlayer = dummyParticipant,
                sactionType = "",
                reason = "",
                appliedTo = dummyParticipant,
                durationTime = durStr
            )
        }
        is Substitution -> {
            MatchPeriodEventsInner(
                happenedAtTime = secondsOfDay,
                atLocalScore = atLocalScore,
                atVisitorScore = atVisitorScore,
                responsibleTeamId = respTeamId,
                incomingPlayer = incomingPlayer.participant.toDomain().toDetailsDTO(),
                outgoingPlayer = outgoingPlayer.participant.toDomain().toDetailsDTO(),
                sactionType = "",
                reason = "",
                appliedTo = dummyParticipant,
                durationTime = "00:00"
            )
        }
        is Sanction -> {
            MatchPeriodEventsInner(
                happenedAtTime = secondsOfDay,
                atLocalScore = atLocalScore,
                atVisitorScore = atVisitorScore,
                responsibleTeamId = respTeamId,
                incomingPlayer = dummyParticipant,
                outgoingPlayer = dummyParticipant,
                sactionType = sanctionType.sanctionTypeName,
                reason = reason,
                appliedTo = appliedTo.participant.toDomain().toDetailsDTO(),
                durationTime = "00:00"
            )
        }
        else -> throw IllegalArgumentException("Unknown event type")
    }
}