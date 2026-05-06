package es.uib.tfg.sports_league_backend.league.infrastructure.mapper

import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sportsapi.dto.ConfigurationDetails
import es.uib.tfg.sportsapi.dto.LeagueCategory
import es.uib.tfg.sportsapi.dto.LeagueDetails
import es.uib.tfg.sportsapi.dto.LeagueSummary
import java.net.URI
import java.time.LocalDate

fun League.toDetailsDTO(): LeagueDetails =
    LeagueDetails(
        leagueId = this.id,
        name = this.name,
        description = "",
        iconImageUrl = URI(""),
        bannerImageUrl = URI(""),
        locationUrl = URI(""),
        startDate = this.startDate,
        endDate = this.endDate,
        maxInscriptionDate = LocalDate.now(),
        status = this.status,
        createdAt = this.createdAt,
        configuration = ConfigurationDetails(LeagueCategory.BOTH, 0, 0, 0, "Voley"),
        punctuationSystem = null,
    )

fun League.toSummaryDTO(): LeagueSummary {
    return LeagueSummary(
        id,
        name,
        description,
        URI(iconImageUrl ?: ""),
        URI(bannerImageUrl ?: ""),
        URI(locationUrl ?: ""),
        startDate,
        endDate,
        maxInscriptionDate,
        status,
    )
}