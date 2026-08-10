package es.uib.tfg.sports_league_backend.league.application.ports.`in`

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueRetrieveError
import es.uib.tfg.sports_league_backend.match.domain.Match
import es.uib.tfg.sports_league_backend.match.domain.Proposal
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sportsapi.dto.LeaderboardGroup
import es.uib.tfg.sportsapi.dto.MatchState

interface LeagueQueryUseCase {
    fun getLeaderboard(leagueId: Long, phaseId: Long?, roundId: Long?): DomainResult<List<LeaderboardGroup>, LeagueRetrieveError>
    fun findAllParticipantsByLeagueId(leagueId: Long): DomainResult<List<Participant>, LeagueRetrieveError>
    fun getActiveProposal(matchId: Long): Proposal?
    fun findMatches(leagueId: Long, phaseId: Long?, teamId: Long?, status: MatchState?): List<Match>
}
