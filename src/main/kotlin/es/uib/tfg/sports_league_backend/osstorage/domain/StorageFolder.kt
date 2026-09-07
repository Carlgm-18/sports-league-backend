package es.uib.tfg.sports_league_backend.osstorage.domain

enum class StorageFolder(val folderName: String) {
    AVATARS("avatars"),
    PROFILES("profiles"),
    LEAGUE_BANNERS("league-banners"),
    TEAM_SHIELDS("team-shields"),
    SIGNATURES("signatures"),
    MATCH_RECORDS("match-records");
    companion object {
        fun isValid(folder: String): Boolean {
            return entries.any { it.folderName.equals(folder, ignoreCase = true) }
        }

        fun getAllFolderNames(): List<String> {
            return entries.map { it.folderName }
        }
    }
}
