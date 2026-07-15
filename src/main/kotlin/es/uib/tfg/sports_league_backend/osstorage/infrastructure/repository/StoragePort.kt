package es.uib.tfg.sports_league_backend.osstorage.infrastructure.repository

import es.uib.tfg.sports_league_backend.osstorage.infrastructure.controller.StorageUrlResponse


interface StoragePort {
    fun generateUploadUrl(folder: String, extension: String): StorageUrlResponse
}
