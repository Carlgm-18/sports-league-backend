package es.uib.tfg.sports_league_backend.team

import es.uib.tfg.sports_league_backend.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class TeamIntegrationTests : BaseIntegrationTest() {

    @Test
    fun testTeamLifecycle() {
        val uniqueEmail = randomEmail()
        val token = obtainToken(uniqueEmail)

        // 1. Create league
        val leagueId = createLeague(token, "Liga Test Equipos")

        // 2. Fetch participant ID
        val participantId = getParticipantId(token, leagueId)

        // 3. Create TeamCreateRequest
        val teamRequestJson = """
            {
              "requestType": "TEAM_CREATE",
              "name": "Equipo Test 1",
              "initials": "ET1",
              "description": "El primer equipo de prueba",
              "motto": "A ganar siempre",
              "primaryColor": "#FF0000",
              "secondaryColor": "#0000FF",
              "requestId": 0,
              "participantId": $participantId,
              "leagueId": $leagueId,
              "createdAt": "2026-06-22T12:00:00",
              "status": "PENDING"
            }
        """.trimIndent()

        val teamRequestResponse = mockMvc.perform(
            post("/api/v1/leagues/$leagueId/requests")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(teamRequestJson)
        ).andExpect(status().isCreated)
         .andReturn()
         .response
         .contentAsString

        val teamRequestNode = objectMapper.readTree(teamRequestResponse)
        val requestId = teamRequestNode.get("requestId").asLong()

        // 4. Resolve (Approve) TeamCreateRequest to create the Team
        val resolveJson = """
            {
              "status": "ACCEPTED"
            }
        """.trimIndent()

        val resolveResponse = mockMvc.perform(
            patch("/api/v1/requests/$requestId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resolveJson)
        ).andExpect(status().isCreated)
         .andReturn()
         .response
         .contentAsString

        val resolvedNode = objectMapper.readTree(resolveResponse)
        assert(resolvedNode.get("status").asText() == "ACCEPTED")

        // 5. Fetch teams of the league and check if our team is listed
        mockMvc.perform(
            get("/api/v1/leagues/$leagueId/teams")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)
         .andExpect(jsonPath("$[0].name").value("Equipo Test 1"))
    }
}
