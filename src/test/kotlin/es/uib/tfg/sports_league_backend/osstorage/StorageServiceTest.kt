package es.uib.tfg.sports_league_backend.osstorage

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.osstorage.application.StorageService
import es.uib.tfg.sports_league_backend.osstorage.application.ports.out.StoragePort
import es.uib.tfg.sports_league_backend.osstorage.domain.StorageUrl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StorageServiceTest {

    private val storagePort = mockk<StoragePort>()
    private val storageService = StorageService(storagePort)

    @Test
    fun `should generate upload url successfully`() {
        // Given
        val folder = "avatars"
        val extension = "png"
        val expectedUrl = StorageUrl("http://upload.url", "http://public.url")
        every { storagePort.generateUploadUrl(folder, extension) } returns expectedUrl

        // When
        val result = storageService.getUploadUrl(folder, extension)

        // Then
        assertEquals(expectedUrl, (result as? DomainResult.Success)?.data)
        verify(exactly = 1) { storagePort.generateUploadUrl(folder, extension) }
    }
}
