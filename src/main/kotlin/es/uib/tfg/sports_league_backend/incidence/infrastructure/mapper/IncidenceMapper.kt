package es.uib.tfg.sports_league_backend.incidence.infrastructure.mapper

import es.uib.tfg.sports_league_backend.incidence.domain.Incidence
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sportsapi.dto.IncidenceCreateRequest
import es.uib.tfg.sportsapi.dto.IncidenceDetails

fun Incidence.toDTO(): IncidenceDetails =
    IncidenceDetails(
        incidenceId = id!!,
        description = description,
        resolution = resolution,
        creator = creator.toSummaryDTO()
    )

fun IncidenceCreateRequest.toEntity(creator: Participant): Incidence =
    Incidence(
        description = description,
        creator = creator,
        leagueId = creator.league.id!!
    )