package es.uib.tfg.sports_league_backend.league.infrastructure.repository

import es.uib.tfg.sports_league_backend.league.domain.PunctuationSystem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PunctuationSystemRepository: JpaRepository<PunctuationSystem, Long> {
}