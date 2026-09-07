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

    protected fun randomEmail(): String = "u_${java.util.UUID.randomUUID().toString().take(8)}@example.com"

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
        val configName = "Config_${java.util.UUID.randomUUID().toString().take(8)}"
        val punctuationName = "Punct_${java.util.UUID.randomUUID().toString().take(8)}"
        val startDate = java.time.LocalDate.now().plusDays(10).toString()
        val endDate = java.time.LocalDate.now().plusMonths(3).toString()
        val maxInscriptionDate = java.time.LocalDate.now().plusDays(5).toString()
        val phaseEndDate = java.time.LocalDate.now().plusMonths(2).toString()
        val leagueJson = """
            {
              "name": "$name",
              "description": "Una gran liga de voleibol de verano",
              "locationUrl": "http://example.com/polideportivo",
              "startDate": "$startDate",
              "endDate": "$endDate",
              "maxInscriptionDate": "$maxInscriptionDate",
              "customConfiguration": {
                "name": "$configName",
                "category": "MIXT",
                "minTeamMembers": 6,
                "maxTeamMembers": 25,
                "roundDuration": 1,
                "sportId": 1
              },
              "customPunctuationSystem": {
                "name": "$punctuationName",
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
                  "startDate": "$startDate",
                  "endDate": "$phaseEndDate",
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
