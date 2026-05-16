package es.uib.tfg.sports_league_backend.league.infrastructure.controller

import es.uib.tfg.sportsapi.dto.LeagueCreateRequest

fun LeagueCreateRequest.isValidConfiguration(): Boolean =
    ((customConfiguration == null) xor (configurationId == null))

fun LeagueCreateRequest.isValidPunctuationSystem(): Boolean =
    ((customPunctuationSystem == null) xor (punctuationSystemId == null))