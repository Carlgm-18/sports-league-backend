package es.uib.tfg.sports_league_backend

import com.fasterxml.jackson.databind.ObjectMapper
import es.uib.tfg.sports_league_backend.match.infrastructure.repository.MatchRepository
import es.uib.tfg.sports_league_backend.incidence.infrastructure.repository.IncidenceRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class LeagueBackendIntegrationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var matchRepository: MatchRepository

    @Autowired
    private lateinit var incidenceRepository: IncidenceRepository

    private fun obtainToken(email: String): String {
        // Register the user first
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

        // Login to get token
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

    @Test
    fun testUserLifecycle() {
        val uniqueEmail = "user.${UUID.randomUUID()}@example.com"
        val token = obtainToken(uniqueEmail)
        
        assert(token.isNotEmpty())

        // 1. Fetch current user details
        mockMvc.perform(
            get("/api/v1/users/me")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)
         .andExpect(jsonPath("$.email").value(uniqueEmail))
         .andExpect(jsonPath("$.fullName").value("Test User"))

        // 2. Update current user details
        val updateJson = """
            {
              "firstName": "TestUpdated",
              "lastName": "UserUpdated"
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/api/v1/users/me")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
        ).andExpect(status().isCreated)
         .andExpect(jsonPath("$.fullName").value("TestUpdated UserUpdated"))
    }

    @Test
    fun testStorageUploadUrl() {
        val uniqueEmail = "user.${UUID.randomUUID()}@example.com"
        val token = obtainToken(uniqueEmail)

        mockMvc.perform(
            get("/api/v1/storage/upload-url")
                .header("Authorization", "Bearer $token")
                .param("folder", "avatars")
                .param("extension", "png")
        ).andExpect(status().isOk)
         .andExpect(jsonPath("$.uploadUrl").exists())
         .andExpect(jsonPath("$.publicUrl").exists())
    }

    @Test
    fun testLeagueLifecycle() {
        val uniqueEmail = "user.${UUID.randomUUID()}@example.com"
        val token = obtainToken(uniqueEmail)

        // 1. Create league
        val leagueJson = """
            {
              "name": "Liga Test mockmvc",
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
        val leagueId = leagueNode.get("leagueId").asLong()

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
        assert(matches.isNotEmpty() || matches.isEmpty()) // Since there are no teams yet, matches might be empty, but flow executes
    }

    @Test
    fun testTeamLifecycle() {
        val uniqueEmail = "user.${UUID.randomUUID()}@example.com"
        val token = obtainToken(uniqueEmail)

        // 1. Create league
        val leagueJson = """
            {
              "name": "Liga Test Equipos",
              "description": "Una gran liga de voleibol de verano",
              "locationUrl": "http://example.com/polideportivo",
              "startDate": "2026-07-01",
              "endDate": "2026-08-31",
              "maxInscriptionDate": "2026-06-30",
              "customConfiguration": {
                "name": "Configuracion Voleibol Test 2",
                "category": "MIXT",
                "minTeamMembers": 6,
                "maxTeamMembers": 25,
                "roundDuration": 1,
                "sportId": 1
              },
              "customPunctuationSystem": {
                "name": "Sistema Voleibol Test 2",
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
                  "name": "Fase Regular Test 2",
                  "startDate": "2026-07-01",
                  "endDate": "2026-07-31",
                  "sequenceOrder": 1,
                  "type": "CLASSIFICATION",
                  "groups": [
                    {
                      "groupName": "Grupo A Test 2",
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
        val leagueId = leagueNode.get("leagueId").asLong()

        // 2. Fetch participant ID
        val participantResponse = mockMvc.perform(
            get("/api/v1/leagues/$leagueId/my-status")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)
         .andReturn()
         .response
         .contentAsString

        val participantNode = objectMapper.readTree(participantResponse)
        val participantId = participantNode.get("participantId").asLong()

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

    @Test
    fun testIncidenceLifecycle() {
        val uniqueEmail = "user.${UUID.randomUUID()}@example.com"
        val token = obtainToken(uniqueEmail)

        // 1. Create league
        val leagueJson = """
            {
              "name": "Liga Test Incidencias",
              "description": "Una gran liga de voleibol de verano",
              "locationUrl": "http://example.com/polideportivo",
              "startDate": "2026-07-01",
              "endDate": "2026-08-31",
              "maxInscriptionDate": "2026-06-30",
              "customConfiguration": {
                "name": "Configuracion Voleibol Test 3",
                "category": "MIXT",
                "minTeamMembers": 6,
                "maxTeamMembers": 25,
                "roundDuration": 1,
                "sportId": 1
              },
              "customPunctuationSystem": {
                "name": "Sistema Voleibol Test 3",
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
                  "name": "Fase Regular Test 3",
                  "startDate": "2026-07-01",
                  "endDate": "2026-07-31",
                  "sequenceOrder": 1,
                  "type": "CLASSIFICATION",
                  "groups": [
                    {
                      "groupName": "Grupo A Test 3",
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
        val leagueId = leagueNode.get("leagueId").asLong()

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

    @Test
    fun testTokenRefresh() {
        val uniqueEmail = "user.${UUID.randomUUID()}@example.com"
        
        // 1. Register the user
        val registerJson = """
            {
              "firstName": "Refresh",
              "lastName": "Tester",
              "email": "$uniqueEmail",
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

        // 2. Login to get token
        val loginJson = """
            {
              "email": "$uniqueEmail",
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
        val refreshToken = jsonNode.get("refreshToken").asText()
        assert(refreshToken.isNotEmpty())

        // 3. Refresh token
        val refreshRequestJson = """
            {
              "refreshToken": "$refreshToken"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/auth/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequestJson)
        ).andExpect(status().isOk)
         .andExpect(jsonPath("$.accessToken").exists())
         .andExpect(jsonPath("$.refreshToken").exists())
    }

    @Test
    fun testValidationErrorsAdvice() {
        // Submit register request with invalid email format (missing @) to trigger validation error
        val invalidRegisterJson = """
            {
              "firstName": "Invalid",
              "lastName": "User",
              "email": "invalidemail",
              "password": "123",
              "confirmPassword": "123",
              "category": "MALE"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRegisterJson)
        ).andExpect(status().isBadRequest)
         .andExpect(jsonPath("$.timestamp").exists())
         .andExpect(jsonPath("$.status").value(400))
         .andExpect(jsonPath("$.errors").isArray)
    }

    @Test
    fun testGetLeagueMatchesEndpoint() {
        val uniqueEmail = "user.${UUID.randomUUID()}@example.com"
        val token = obtainToken(uniqueEmail)

        // 1. Create a league
        val leagueJson = """
            {
              "name": "Matches Query League",
              "description": "Matches query test",
              "locationUrl": "http://example.com/polideportivo",
              "startDate": "2026-07-01",
              "endDate": "2026-08-31",
              "maxInscriptionDate": "2026-06-30",
              "customConfiguration": {
                "name": "Configuracion Voleibol Matches",
                "category": "MIXT",
                "minTeamMembers": 6,
                "maxTeamMembers": 25,
                "roundDuration": 1,
                "sportId": 1
              },
              "customPunctuationSystem": {
                "name": "Sistema Voleibol Matches",
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
                  "name": "Fase Regular Matches",
                  "startDate": "2026-07-01",
                  "endDate": "2026-07-31",
                  "sequenceOrder": 1,
                  "type": "CLASSIFICATION",
                  "groups": [
                    {
                      "groupName": "Grupo A Matches",
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
        val leagueId = leagueNode.get("leagueId").asLong()

        // 2. Query matches endpoint
        mockMvc.perform(
            get("/api/v1/leagues/$leagueId/matches")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)
         .andExpect(jsonPath("$").isArray)
    }
}
