package es.uib.tfg.sports_league_backend.core.osstorage;


data class StorageUrlResponse(
        val uploadUrl: String,
        val publicUrl: String
)

interface StoragePort {
    // folder puede ser "avatars", "league-banners", "team-shields"...
    fun generateUploadUrl(folder: String, extension: String): StorageUrlResponse
}
