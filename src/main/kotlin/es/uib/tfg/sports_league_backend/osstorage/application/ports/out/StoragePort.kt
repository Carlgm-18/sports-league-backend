package es.uib.tfg.sports_league_backend.osstorage.application.ports.out

import es.uib.tfg.sports_league_backend.osstorage.domain.StorageUrl

interface StoragePort {
    fun generateUploadUrl(folder: String, extension: String): StorageUrl
}
