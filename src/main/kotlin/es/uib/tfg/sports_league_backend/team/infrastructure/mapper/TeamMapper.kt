package es.uib.tfg.sports_league_backend.team.infrastructure.mapper

import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sportsapi.dto.TeamSummary
import java.net.URI

fun Team.toSummaryDTO(): TeamSummary =
    TeamSummary(
        id!!,
        name,
        initials,
        motto ?: "",
        primaryColor ?: "#FFFFFF",
        secondaryColor ?: "#FFFFFF",
        URI(iconImageUrl ?: "")
    )