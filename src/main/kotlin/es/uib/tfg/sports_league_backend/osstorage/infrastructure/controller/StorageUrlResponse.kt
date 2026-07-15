package es.uib.tfg.sports_league_backend.osstorage.infrastructure.controller

data class StorageUrlResponse(
    val uploadUrl: String,
    val publicUrl: String
)