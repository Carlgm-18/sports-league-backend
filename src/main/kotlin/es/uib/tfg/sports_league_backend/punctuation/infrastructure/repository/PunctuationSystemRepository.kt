package es.uib.tfg.sports_league_backend.punctuation.infrastructure.repository

import es.uib.tfg.sports_league_backend.punctuation.domain.PunctuationSystem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PunctuationSystemRepository: JpaRepository<PunctuationSystem, Long> {
}