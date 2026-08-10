package es.uib.tfg.sports_league_backend.osstorage.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.osstorage.application.ports.`in`.GetUploadUrlUseCase
import es.uib.tfg.sports_league_backend.osstorage.application.ports.out.StoragePort
import es.uib.tfg.sports_league_backend.osstorage.domain.StorageUrl
import es.uib.tfg.sports_league_backend.osstorage.domain.errors.StorageUploadError
import org.springframework.stereotype.Service

@Service
class StorageService(
    private val storagePort: StoragePort
) : GetUploadUrlUseCase {
    override fun getUploadUrl(folder: String, extension: String): DomainResult<StorageUrl, StorageUploadError> {
        return DomainResult.Success(storagePort.generateUploadUrl(folder, extension))
    }
}