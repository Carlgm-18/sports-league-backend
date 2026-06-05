package es.uib.tfg.sports_league_backend.user.infrastructure.mapper

import es.uib.tfg.sports_league_backend.sport.infrastructure.mapper.toDetails
import es.uib.tfg.sports_league_backend.sport.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.user.domain.RefereeLicense
import es.uib.tfg.sportsapi.dto.LicenseElement

fun LicenseElement.toEntity(): RefereeLicense =
    RefereeLicense(
        license = license,
        sport = sport.toEntity()
    )

fun RefereeLicense.toDTO(): LicenseElement =
    LicenseElement(
        sport.toDetails(),
        license
    )