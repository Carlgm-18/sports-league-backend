package es.uib.tfg.sports_league_backend.incidence.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.incidence.domain.Incidence
import es.uib.tfg.sports_league_backend.incidence.domain.errors.IncidenceCreateError
import es.uib.tfg.sports_league_backend.incidence.domain.errors.IncidenceNotFound
import es.uib.tfg.sports_league_backend.incidence.domain.errors.IncidenceRetrieveError
import es.uib.tfg.sports_league_backend.incidence.domain.errors.ParticipantNotFound
import es.uib.tfg.sports_league_backend.incidence.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.incidence.infrastructure.repository.IncidenceRepository
import es.uib.tfg.sports_league_backend.participant.application.ParticipantService
import es.uib.tfg.sportsapi.dto.IncidenceCreateRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class IncidenceService(
    val incidenceRepository: IncidenceRepository,
    private val participantService: ParticipantService,
) {
    fun save(userId: Long, leagueId: Long, request: IncidenceCreateRequest): DomainResult<Incidence, IncidenceCreateError> {
        val participant = when(val result = participantService.findParticipant(userId, leagueId)) {
            is DomainResult.Success -> result.data
            is DomainResult.Failure -> return DomainResult.Failure(ParticipantNotFound)
        }

        val incidence = request.toEntity(participant)

        return DomainResult.Success(incidenceRepository.save(incidence))
    }

    fun findIncidenceById(incidenceId: Long): DomainResult<Incidence, IncidenceRetrieveError> =
        incidenceRepository.findByIdOrNull(incidenceId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(IncidenceNotFound)

}