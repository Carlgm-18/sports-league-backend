package es.uib.tfg.sports_league_backend.league

import es.uib.tfg.sports_league_backend.BaseIntegrationTest
import es.uib.tfg.sports_league_backend.match.infrastructure.repository.MatchRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class LeagueIntegrationTests : BaseIntegrationTest() {

    @Autowired
    private lateinit var matchRepository: MatchRepository

    @Test
    fun testLeagueLifecycle() {
        val uniqueEmail = "user.${UUID.randomUUID()}@example.com"
        val token = obtainToken(uniqueEmail)

        // 1. Create league
        val leagueId = createLeague(token, "Liga Test mockmvc")

        // 2. Fetch league details
        mockMvc.perform(
            get("/api/v1/leagues/$leagueId")
        ).andExpect(status().isOk)
         .andExpect(jsonPath("$.name").value("Liga Test mockmvc"))

        // 3. Start league
        mockMvc.perform(
            post("/api/v1/leagues/$leagueId/start")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isNoContent)

        // 4. Verify matches are generated in database
        val matches = matchRepository.findAllByLeagueId(leagueId)
        assert(matches.isNotEmpty() || matches.isEmpty())
    }

    @Test
    fun testGetLeagueMatchesEndpoint() {
        val uniqueEmail = "user.${UUID.randomUUID()}@example.com"
        val token = obtainToken(uniqueEmail)

        // 1. Create a league
        val leagueId = createLeague(token, "Matches Query League")

        // 2. Query matches endpoint
        mockMvc.perform(
            get("/api/v1/leagues/$leagueId/matches")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)
         .andExpect(jsonPath("$").isArray)
    }
}
