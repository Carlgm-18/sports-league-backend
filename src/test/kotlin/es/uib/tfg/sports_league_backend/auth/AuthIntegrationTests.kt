package es.uib.tfg.sports_league_backend.auth

import es.uib.tfg.sports_league_backend.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class AuthIntegrationTests : BaseIntegrationTest() {

    @Test
    fun testTokenRefresh() {
        val uniqueEmail = randomEmail()
        
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
}
