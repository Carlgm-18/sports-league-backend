package es.uib.tfg.sports_league_backend

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
abstract class BaseIntegrationTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    protected fun obtainToken(email: String): String {
        val registerJson = """
            {
              "firstName": "Test",
              "lastName": "User",
              "email": "$email",
              "password": "securepassword123",
              "confirmPassword": "securepassword123",
              "category": "MALE"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson)
        ).andExpect(status().isCreated)

        val loginJson = """
            {
              "email": "$email",
              "password": "securepassword123"
            }
        """.trimIndent()

        val response = mockMvc.perform(
            post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        ).andExpect(status().isOk)
         .andReturn()
         .response
         .contentAsString

        val jsonNode = objectMapper.readTree(response)
        return jsonNode.get("accessToken").asText()
    }

    protected fun createLeague(token: String, name: String): Long {
        val leagueJson = """
            {
              "name": "$name",
              "description": "Una gran liga de voleibol de verano",
              "locationUrl": "http://example.com/polideportivo",
              "startDate": "2026-07-01",
              "endDate": "2026-08-31",
              "maxInscriptionDate": "2026-06-30",
              "customConfiguration": {
                "name": "Configuracion Voleibol Test",
                "category": "MIXT",
                "minTeamMembers": 6,
                "maxTeamMembers": 25,
                "roundDuration": 1,
                "sportId": 1
              },
              "customPunctuationSystem": {
                "name": "Sistema Voleibol Test",
                "rules": [
                  {
                    "localScore": 3,
                    "visitorScore": 0,
                    "localPoints": 3,
                    "visitorPoints": 0
                  }
                ],
                "sportId": 1
              },
              "phases": [
                {
                  "name": "Fase Regular Test",
                  "startDate": "2026-07-01",
                  "endDate": "2026-07-31",
                  "sequenceOrder": 1,
                  "type": "CLASSIFICATION",
                  "groups": [
                    {
                      "groupName": "Grupo A Test",
                      "topWinners": 2
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val createResponse = mockMvc.perform(
            post("/api/v1/leagues")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(leagueJson)
        ).andExpect(status().isCreated)
         .andReturn()
         .response
         .contentAsString

        val leagueNode = objectMapper.readTree(createResponse)
        return leagueNode.get("leagueId").asLong()
    }

    protected fun getParticipantId(token: String, leagueId: Long): Long {
        val participantResponse = mockMvc.perform(
            get("/api/v1/leagues/$leagueId/my-status")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)
         .andReturn()
         .response
         .contentAsString

        val participantNode = objectMapper.readTree(participantResponse)
        return participantNode.get("participantId").asLong()
    }
}
