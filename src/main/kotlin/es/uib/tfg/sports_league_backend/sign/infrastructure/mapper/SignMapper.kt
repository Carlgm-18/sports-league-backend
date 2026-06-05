package es.uib.tfg.sports_league_backend.sign.infrastructure.mapper

import es.uib.tfg.sports_league_backend.sign.domain.Sign
import es.uib.tfg.sportsapi.dto.SignImageUrl
import java.net.URI

fun Sign.toDTO() =
    SignImageUrl(
        URI(signImageUrl)
    )