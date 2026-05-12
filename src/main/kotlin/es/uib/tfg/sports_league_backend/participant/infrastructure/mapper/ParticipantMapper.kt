package es.uib.tfg.sports_league_backend.participant.infrastructure.mapper

import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.team.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sportsapi.dto.ParticipantSummary

fun Participant.toSummaryDTO(): ParticipantSummary =
    ParticipantSummary(
        id,
        "${user.firstName} ${user.lastName}",
        listOf(),
        team?.toSummaryDTO(),
        dorsal,
    )