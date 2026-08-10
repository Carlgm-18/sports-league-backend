package es.uib.tfg.sports_league_backend.league.application.ports.`in`

import es.uib.tfg.sportsapi.dto.ConfigurationDetails
import es.uib.tfg.sportsapi.dto.ConfigurationUpdateRequest

interface ManageLeagueConfigurationUseCase {
    fun updateConfiguration(leagueId: Long, request: ConfigurationUpdateRequest): ConfigurationDetails
}
