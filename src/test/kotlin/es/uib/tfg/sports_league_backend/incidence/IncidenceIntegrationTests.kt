package es.uib.tfg.sports_league_backend.incidence

import es.uib.tfg.sports_league_backend.BaseIntegrationTest
import es.uib.tfg.sports_league_backend.incidence.infrastructure.repository.IncidenceRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class IncidenceIntegrationTests : BaseIntegrationTest() {

    @Autowired
    private lateinit var incidenceRepository: IncidenceRepository

    @Test
    fun testIncidenceLifecycle() {
        val uniqueEmail = randomEmail()
        val token = obtainToken(uniqueEmail)

        // 1. Create league
        val leagueId = createLeague(token, "Liga Test Incidencias")

        // 2. Create incidence
        val incidenceJson = """
            {
              "description": "Una bombilla de la pista 2 está rota"
            }
        """.trimIndent()

        val incidenceResponse = mockMvc.perform(
            post("/api/v1/leagues/$leagueId/incidences")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incidenceJson)
        ).andExpect(status().isOk)
         .andReturn()
         .response
         .contentAsString

        val incidenceNode = objectMapper.readTree(incidenceResponse)
        val incidenceId = incidenceNode.get("incidenceId").asLong()

        // 3. Verify in repository
        val incidenceOpt = incidenceRepository.findById(incidenceId)
        assert(incidenceOpt.isPresent)
        assert(incidenceOpt.get().description == "Una bombilla de la pista 2 está rota")
    }
}
