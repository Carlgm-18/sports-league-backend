package es.uib.tfg.sports_league_backend.sport.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.sport.domain.Sport
import es.uib.tfg.sports_league_backend.sport.domain.errors.SportRetrieveError
import es.uib.tfg.sports_league_backend.sport.infrastructure.repository.SportRepository
import org.springframework.stereotype.Service

@Service
class SportService(
    private val sportRepository: SportRepository,
) {
    fun getAllSports(): List<Sport> =
        sportRepository.findAll()

    fun getSportById(sportId: Long): DomainResult<Sport, SportRetrieveError> {
        val sport = sportRepository.findById(sportId)
        return if(sport.isPresent) {
            DomainResult.Failure(SportRetrieveError.SportNotFound)
        }else {
            DomainResult.Success(sport.get())
        }

    }
}