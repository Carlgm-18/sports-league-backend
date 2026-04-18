package es.uib.tfg.sports_league_backend.league

import es.uib.tfg.sports_league_backend.league.entities.League
import es.uib.tfg.sportsapi.dto.ConfigurationDetails
import es.uib.tfg.sportsapi.dto.ConfigurationUpdateRequest
import es.uib.tfg.sportsapi.dto.LeagueCreateRequest
import es.uib.tfg.sportsapi.dto.LeagueDetails
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class LeagueService {
    fun findAll(): List<LeagueDetails> {
        // TODO: Implement database logic
        val leagues: List<League> =
            listOf(
                League(id = 1, name = "Liga verano", startDate = LocalDate.now(), endDate = LocalDate.now()),
                League(id = 2, name = "Liga invierno", startDate = LocalDate.now(), endDate = LocalDate.now()),
                League(id = 3, name = "Liga primavera", startDate = LocalDate.now(), endDate = LocalDate.now())
            )

        return leagues.map { it.toDetailsDTO() }
    }

    fun createLeague(request: LeagueCreateRequest): LeagueDetails {
        // TODO: Implement database logic
        TODO("Not yet implemented")
    }

    fun findById(leagueId: Int): LeagueDetails {
        // TODO: Implement database logic
        throw NotImplementedError("Service not yet implemented")
    }

    fun updateConfiguration(
        leagueId: Int,
        request: ConfigurationUpdateRequest
    ): ConfigurationDetails {
        // TODO: Implement database logic
        TODO("Not yet implemented")
    }

}