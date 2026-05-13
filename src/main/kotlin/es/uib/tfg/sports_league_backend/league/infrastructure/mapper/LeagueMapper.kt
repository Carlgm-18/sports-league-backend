package es.uib.tfg.sports_league_backend.league.infrastructure.mapper

import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.domain.LeagueConfiguration
import es.uib.tfg.sports_league_backend.phase.domain.Phase
import es.uib.tfg.sports_league_backend.phase.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.punctuation.domain.PunctuationRule
import es.uib.tfg.sports_league_backend.punctuation.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.punctuation.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.sport.infrastructure.mapper.toDetails
import es.uib.tfg.sports_league_backend.sport.infrastructure.mapper.toEntity
import es.uib.tfg.sportsapi.dto.ConfigurationCreateRequest
import es.uib.tfg.sportsapi.dto.ConfigurationDetails
import es.uib.tfg.sportsapi.dto.LeagueCreateRequest
import es.uib.tfg.sportsapi.dto.LeagueDetails
import es.uib.tfg.sportsapi.dto.LeagueSummary
import es.uib.tfg.sportsapi.dto.PhaseCreateRequest
import java.net.URI

fun League.toDetailsDTO(): LeagueDetails =
    LeagueDetails(
        id!!,
        name,
        description,
        URI(iconImageUrl ?: ""),
        URI(bannerImageUrl ?: ""),
        URI(locationUrl ?: ""),
        startDate,
        endDate,
        maxInscriptionDate,
        status,
        createdAt,
        configuration.toDetailsDTO(),
        punctuationSystem.toDetailsDTO(),
    )

fun League.toSummaryDTO(): LeagueSummary {
    return LeagueSummary(
        id!!,
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

fun LeagueCreateRequest.toEntity(): League =
    League(
        configuration = configuration.toEntity(),
        punctuationSystem = punctuationSystem.toEntity(),
        name = name,
        description = description,
        iconImageUrl = iconImageUrl.toString(),
        bannerImageUrl = bannerImageUrl.toString(),
        locationUrl = locationUrl.toString(),
        startDate = startDate,
        endDate = endDate,
        maxInscriptionDate = maxInscriptionDate,
        phases = phases.map { it.toEntity() }.toMutableList(),
    )

fun ConfigurationCreateRequest.toEntity(): LeagueConfiguration =
    LeagueConfiguration(
        name = name,
        category = category,
        minTeamFemaleIntegrants = minTeamFemaleIntegrants,
        minTeamMembers = minTeamMembers,
        maxTeamMembers = maxTeamMembers,
        roundDuration = roundDuration,
        sport = sport.toEntity(),
    )

fun LeagueConfiguration.toDetailsDTO(): ConfigurationDetails =
    ConfigurationDetails(
        category,
        minTeamMembers,
        maxTeamMembers,
        roundDuration,
        sport.toDetails(),
        minTeamFemaleIntegrants,
    )