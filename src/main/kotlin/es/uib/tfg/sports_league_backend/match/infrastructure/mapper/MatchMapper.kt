package es.uib.tfg.sports_league_backend.match.infrastructure.mapper

import es.uib.tfg.sports_league_backend.match.domain.Match
import es.uib.tfg.sports_league_backend.participant.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sports_league_backend.schedule.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.team.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sportsapi.dto.MatchDetails
import es.uib.tfg.sportsapi.dto.MatchSummary

fun Match.toSummaryDTO(): MatchSummary =
    MatchSummary(
        id!!,
        status,
        1,
        localTeam?.toSummaryDTO(),
        visitorTeam?.toSummaryDTO(),
        dateTime?.toDetailsDTO(),
        if (dateTime == null) MatchSummary.ProposalState.PENDING
        else MatchSummary.ProposalState.PROPOSED,
        firstReferee?.toSummaryDTO(),
        secondReferee?.toSummaryDTO(),
        null,
    )