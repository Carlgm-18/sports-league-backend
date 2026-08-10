package es.uib.tfg.sports_league_backend.osstorage.application.ports.`in`

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.osstorage.domain.StorageUrl
import es.uib.tfg.sports_league_backend.osstorage.domain.errors.StorageUploadError

interface GetUploadUrlUseCase {
    fun getUploadUrl(folder: String, extension: String): DomainResult<StorageUrl, StorageUploadError>
}
