package es.uib.tfg.sports_league_backend.participant.application.ports.`in`

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantJoinError
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sports_league_backend.league.domain.League

interface RegisterParticipantUseCase {
    fun registerOwner(user: User, league: League)
    fun registerPlayer(user: User, league: League): DomainResult<Participant, ParticipantJoinError>
}
