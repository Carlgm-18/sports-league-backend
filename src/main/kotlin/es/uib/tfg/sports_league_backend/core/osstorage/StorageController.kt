package es.uib.tfg.sports_league_backend.core.osstorage

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

// Controller genérico para pedir URLs desde React Native
@RestController
@RequestMapping("/api/v1/storage")
class StorageController(
    private val storagePort: StoragePort // Inyectamos la interfaz, Spring pone el MinioStorageAdapter
) {

    @GetMapping("/upload-url")
    fun getUploadUrl(
        @RequestParam folder: String,
        @RequestParam extension: String
    ): ResponseEntity<StorageUrlResponse> {

        // Validación básica de seguridad para que no suban cosas a carpetas raras
        val allowedFolders = listOf("avatars", "league-banners", "team-shields")
        if (folder !in allowedFolders) {
            return ResponseEntity.badRequest().build()
        }

        val urlInfo = storagePort.generateUploadUrl(folder, extension)
        return ResponseEntity.ok(urlInfo)
    }
}