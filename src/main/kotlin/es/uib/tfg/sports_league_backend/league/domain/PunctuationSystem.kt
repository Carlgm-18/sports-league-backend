package es.uib.tfg.sports_league_backend.league.domain

import es.uib.tfg.sports_league_backend.sport.domain.Sport

class PunctuationSystem (
    var id: Long? = null,
    var name: String,
    var punctuationRules: MutableList<PunctuationRule> = mutableListOf(),
    var sport: Sport,
)
