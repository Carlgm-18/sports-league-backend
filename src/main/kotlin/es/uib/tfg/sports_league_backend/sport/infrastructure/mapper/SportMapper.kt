package es.uib.tfg.sports_league_backend.sport.infrastructure.mapper

import es.uib.tfg.sports_league_backend.sport.domain.Sport
import es.uib.tfg.sportsapi.dto.SportDetails

fun Sport.toDetails(): SportDetails =
    SportDetails(
        id,
        sportName
    )