package es.uib.tfg.sports_league_backend.phase.infrastructure.mapper

import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.match.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.phase.domain.ClassificationGroup
import es.uib.tfg.sports_league_backend.phase.domain.ClassificationPhase
import es.uib.tfg.sports_league_backend.phase.domain.TournamentPhase
import es.uib.tfg.sports_league_backend.phase.domain.TournamentSlot
import es.uib.tfg.sports_league_backend.team.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sportsapi.dto.ClassificationGroupDetails
import es.uib.tfg.sportsapi.dto.ClassificationPhaseCreateRequest
import es.uib.tfg.sportsapi.dto.ClassificationPhaseDetails
import es.uib.tfg.sportsapi.dto.PhaseType
import es.uib.tfg.sportsapi.dto.TournamentPhaseCreateRequest
import es.uib.tfg.sportsapi.dto.TournamentPhaseDetails
import es.uib.tfg.sportsapi.dto.TournamentSlotDetails

fun ClassificationPhaseCreateRequest.toEntity(league: League): ClassificationPhase =
    ClassificationPhase(
        league,
        name,
        startDate,
        endDate,
        sequenceOrder,
    )

fun TournamentPhaseCreateRequest.toEntity(league: League): TournamentPhase =
    TournamentPhase(
        league,
        name,
        startDate,
        endDate,
        sequenceOrder,
    )

fun ClassificationPhase.toDetailsDTO(): ClassificationPhaseDetails =
    ClassificationPhaseDetails(
        id,
        name,
        startDate,
        endDate,
        sequenceOrder,
        PhaseType.CLASSIFICATION,
        null,
        groups.toList().map { it.toDetailsDTO() }

    )

fun ClassificationGroup.toDetailsDTO(): ClassificationGroupDetails =
    ClassificationGroupDetails(
        topWinners,
        groupTeams.map { it.team.toSummaryDTO()  }
    )

fun TournamentPhase.toDetailsDTO(): TournamentPhaseDetails =
    TournamentPhaseDetails(
        id,
        name,
        startDate,
        endDate,
        sequenceOrder,
        PhaseType.TOURNAMENT,
        matchesOrder.map { it.toDetailsDTO() },
        stagesNumber
    )

fun TournamentSlot.toDetailsDTO(): TournamentSlotDetails =
    TournamentSlotDetails(
        indexOrder,
        match.toDetailsDTO()
    )