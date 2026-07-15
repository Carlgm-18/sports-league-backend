package es.uib.tfg.sports_league_backend.osstorage.infrastructure.controller

import es.uib.tfg.sports_league_backend.osstorage.infrastructure.repository.StoragePort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/storage")
class StorageController(
    private val storagePort: StoragePort
) {

    @GetMapping("/upload-url")
    fun getUploadUrl(
        @RequestParam folder: String,
        @RequestParam extension: String
    ): ResponseEntity<StorageUrlResponse> {

        val allowedFolders = listOf("avatars", "league-banners", "team-shields")
        if (folder !in allowedFolders) {
            return ResponseEntity.badRequest().build()
        }

        val urlInfo = storagePort.generateUploadUrl(folder, extension)
        return ResponseEntity.ok(urlInfo)
    }
}