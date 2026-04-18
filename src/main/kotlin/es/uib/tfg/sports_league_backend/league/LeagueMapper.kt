package es.uib.tfg.sports_league_backend.league

import es.uib.tfg.sports_league_backend.league.entities.League
import es.uib.tfg.sportsapi.dto.ConfigurationDetails
import es.uib.tfg.sportsapi.dto.LeagueCategory
import es.uib.tfg.sportsapi.dto.LeagueDetails
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

fun League.toDetailsDTO(): LeagueDetails {
    return LeagueDetails(
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
}