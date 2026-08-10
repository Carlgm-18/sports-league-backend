package es.uib.tfg.sports_league_backend.user

import es.uib.tfg.sports_league_backend.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class UserIntegrationTests : BaseIntegrationTest() {

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
    fun testValidationErrorsAdvice() {
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
}
