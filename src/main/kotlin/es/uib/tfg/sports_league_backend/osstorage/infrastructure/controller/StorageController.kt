package es.uib.tfg.sports_league_backend.osstorage.infrastructure.controller

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.osstorage.application.ports.`in`.GetUploadUrlUseCase
import es.uib.tfg.sports_league_backend.osstorage.domain.StorageFolder
import es.uib.tfg.sports_league_backend.osstorage.infrastructure.mapper.toResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/storage")
class StorageController(
    private val getUploadUrlUseCase: GetUploadUrlUseCase
) {

    @PostMapping("/upload-url")
    fun getUploadUrl(
        @RequestParam folder: String,
        @RequestParam extension: String
    ): ResponseEntity<StorageUrlResponse> {

        if (!StorageFolder.isValid(folder)) {
            return ResponseEntity.badRequest().build()
        }

        return when (val result = getUploadUrlUseCase.getUploadUrl(folder, extension)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toResponse())
            is DomainResult.Failure ->
                ResponseEntity.badRequest().build()
        }
    }
}