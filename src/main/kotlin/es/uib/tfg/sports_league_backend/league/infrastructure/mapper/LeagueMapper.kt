package es.uib.tfg.sports_league_backend.league.infrastructure.mapper

import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.domain.LeagueConfiguration
import es.uib.tfg.sports_league_backend.phase.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.league.domain.PunctuationSystem
import es.uib.tfg.sports_league_backend.sport.domain.Sport
import es.uib.tfg.sports_league_backend.sport.infrastructure.mapper.toDetails
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sportsapi.dto.ConfigurationCreateRequest
import es.uib.tfg.sportsapi.dto.ConfigurationDetails
import es.uib.tfg.sportsapi.dto.LeagueCreateRequest
import es.uib.tfg.sportsapi.dto.LeagueDetails
import es.uib.tfg.sportsapi.dto.LeagueSummary
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

fun LeagueCreateRequest.toEntity(
    configuration: LeagueConfiguration,
    punctuationSystem: PunctuationSystem,
    userEntity: User,
): League {
    val league = League(
        configuration = configuration,
        punctuationSystem = punctuationSystem,
        name = name,
        description = description,
        iconImageUrl = iconImageUrl.toString(),
        bannerImageUrl = bannerImageUrl.toString(),
        locationUrl = locationUrl.toString(),
        startDate = startDate,
        endDate = endDate,
        maxInscriptionDate = maxInscriptionDate,
        phases = phases.map { it.toEntity() }.toMutableList(),
        owner = userEntity
    )

    league.phases.forEach { it.league = league }

    return league
}

fun ConfigurationCreateRequest.toEntity(sport: Sport): LeagueConfiguration =
    LeagueConfiguration(
        name = name,
        category = category,
        minTeamFemaleIntegrants = minTeamFemaleIntegrants,
        minTeamMembers = minTeamMembers,
        maxTeamMembers = maxTeamMembers,
        roundDuration = roundDuration,
        sport = sport,
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