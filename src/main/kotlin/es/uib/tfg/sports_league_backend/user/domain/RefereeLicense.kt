package es.uib.tfg.sports_league_backend.user.domain

import es.uib.tfg.sports_league_backend.sport.domain.Sport
import java.time.LocalDateTime

class RefereeLicense (
    var id: Long? = null,
    var license: String,
    var uploadAt: LocalDateTime = LocalDateTime.now(),
    var sport: Sport
)