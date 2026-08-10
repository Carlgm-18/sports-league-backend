package es.uib.tfg.sports_league_backend.phase.infrastructure.mapper

import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueJPAEntity
import es.uib.tfg.sports_league_backend.match.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sports_league_backend.phase.domain.ClassificationGroup
import es.uib.tfg.sports_league_backend.phase.domain.ClassificationPhase
import es.uib.tfg.sports_league_backend.phase.domain.Phase
import es.uib.tfg.sports_league_backend.phase.domain.TournamentPhase
import es.uib.tfg.sports_league_backend.phase.domain.TournamentSlot
import es.uib.tfg.sports_league_backend.team.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sportsapi.dto.ClassificationGroupCreateRequest
import es.uib.tfg.sportsapi.dto.ClassificationGroupDetails
import es.uib.tfg.sportsapi.dto.ClassificationPhaseCreateRequest
import es.uib.tfg.sportsapi.dto.ClassificationPhaseDetails
import es.uib.tfg.sportsapi.dto.PhaseCreateRequest
import es.uib.tfg.sportsapi.dto.PhaseType
import es.uib.tfg.sportsapi.dto.TournamentPhaseCreateRequest
import es.uib.tfg.sportsapi.dto.TournamentPhaseDetails
import es.uib.tfg.sportsapi.dto.TournamentSlotDetails

fun ClassificationPhase.toDetailsDTO(): ClassificationPhaseDetails =
    ClassificationPhaseDetails(
        id!!,
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
        name,
        topWinners,
        groupTeams.map { it.team.toSummaryDTO() }
    )

fun ClassificationGroupCreateRequest.toEntity(): ClassificationGroup =
    ClassificationGroup(
        name = groupName,
        topWinners = topWinners,
    )

fun TournamentPhase.toDetailsDTO(): TournamentPhaseDetails =
    TournamentPhaseDetails(
        id!!,
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
        match.toSummaryDTO()
    )

fun PhaseCreateRequest.toEntity(league: LeagueJPAEntity): Phase =
    when (type) {
        PhaseType.CLASSIFICATION ->
            (this as ClassificationPhaseCreateRequest).toEntity(league)

        PhaseType.TOURNAMENT ->
            (this as TournamentPhaseCreateRequest).toEntity(league)
    }

fun ClassificationPhaseCreateRequest.toEntity(league: LeagueJPAEntity): Phase =
    ClassificationPhase(
        name = name,
        startDate = startDate,
        endDate = endDate,
        sequenceOrder = sequenceOrder,
        groups = groups!!.map { it.toEntity() },
        league = league
    )

fun TournamentPhaseCreateRequest.toEntity(league: LeagueJPAEntity): Phase =
    TournamentPhase(
        name = name,
        startDate = startDate,
        endDate = endDate,
        sequenceOrder = sequenceOrder,
        stagesNumber = stagesNumber,
        league = league
    )