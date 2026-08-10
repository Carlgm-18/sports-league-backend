package es.uib.tfg.sports_league_backend.league.domain

import es.uib.tfg.sports_league_backend.sport.domain.Sport
import es.uib.tfg.sportsapi.dto.LeagueCategory

class LeagueConfiguration (
    var id: Long? = null,
    var name: String = "",
    var category: LeagueCategory,
    var minTeamFemaleIntegrants: Int? = null,
    var minTeamMembers: Int,
    var maxTeamMembers: Int,
    var roundDuration: Int,
    var sport: Sport
)