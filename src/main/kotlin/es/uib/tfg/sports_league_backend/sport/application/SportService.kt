package es.uib.tfg.sports_league_backend.sport.application

import es.uib.tfg.sports_league_backend.sport.domain.Sport
import es.uib.tfg.sports_league_backend.sport.infrastructure.repository.SportRepository
import org.springframework.stereotype.Service

@Service
class SportService(
    private val sportRepository: SportRepository,
) {
    fun getAllSports(): List<Sport> =
        sportRepository.findAll()
}