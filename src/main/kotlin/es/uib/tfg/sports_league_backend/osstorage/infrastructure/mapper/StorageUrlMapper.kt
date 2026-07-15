package es.uib.tfg.sports_league_backend.osstorage.infrastructure.mapper

import es.uib.tfg.sports_league_backend.osstorage.infrastructure.controller.StorageUrlResponse
import es.uib.tfg.sports_league_backend.osstorage.domain.StorageUrl

fun StorageUrl.toResponse() = StorageUrlResponse(uploadUrl, publicUrl)