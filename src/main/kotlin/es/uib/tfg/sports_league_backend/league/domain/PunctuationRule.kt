package es.uib.tfg.sports_league_backend.league.domain

class PunctuationRule(
    var id: Long? = null,
    var punctuationSystem: PunctuationSystem? = null,
    var localScore: Int,
    var visitorScore: Int,
    var localPoints: Int,
    var visitorPoints: Int
)