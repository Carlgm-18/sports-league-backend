package es.uib.tfg.sports_league_backend.osstorage

import es.uib.tfg.sports_league_backend.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class StorageIntegrationTests : BaseIntegrationTest() {

    @Test
    fun testStorageUploadUrl() {
        val uniqueEmail = randomEmail()
        val token = obtainToken(uniqueEmail)

        mockMvc.perform(
            post("/api/v1/storage/upload-url")
                .header("Authorization", "Bearer $token")
                .param("folder", "avatars")
                .param("extension", "png")
        ).andExpect(status().isOk)
         .andExpect(jsonPath("$.uploadUrl").exists())
         .andExpect(jsonPath("$.publicUrl").exists())
    }
}
