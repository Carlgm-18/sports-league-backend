package es.uib.tfg.sports_league_backend.round.infrastructure.repository

import es.uib.tfg.sports_league_backend.round.domain.Round
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoundRepository : JpaRepository<Round, Long> {
}