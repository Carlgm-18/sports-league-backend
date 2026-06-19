package es.uib.tfg.sports_league_backend.team.infrastructure.repository

interface LeaderboardProjection {
    fun getTeamId(): Long
    fun getPlayedMatches(): Int
    fun getWonMatches(): Int
    fun getLostMatches(): Int
    fun getDrawnMatches(): Int
    fun getPoints(): Int
    fun getWonSets(): Int
    fun getLostSets(): Int
    fun getWonPoints(): Int
    fun getLostPoints(): Int
}
