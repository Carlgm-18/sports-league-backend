package es.uib.tfg.sports_league_backend.league.infrastructure.controller

import es.uib.tfg.sportsapi.dto.LeagueCreateRequest

fun LeagueCreateRequest.isValidConfiguration(): Boolean =
    ((customConfiguration != null) xor (configurationId != null))

//private fun LeagueCreateRequest.exlcusiveConfiguration(): Boolean =
//    (customConfiguration != null &&) xor (configurationId != null)
//
//private fun LeagueCreateRequest.minMaxTeamMembersValidation

fun LeagueCreateRequest.isValidPunctuationSystem(): Boolean =
    ((customPunctuationSystem == null) xor (punctuationSystemId == null))