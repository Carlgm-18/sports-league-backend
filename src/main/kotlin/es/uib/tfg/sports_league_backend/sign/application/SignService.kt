package es.uib.tfg.sports_league_backend.sign.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.sign.domain.Sign
import es.uib.tfg.sports_league_backend.sign.domain.errors.SignNotFound
import es.uib.tfg.sports_league_backend.sign.domain.errors.SignRetrieveError
import es.uib.tfg.sports_league_backend.sign.infrastructure.repository.SignRepository
import org.springframework.stereotype.Service

@Service
class SignService(
    private val signRepository: SignRepository,
) {
    fun getSignByUserId(userId: Long): DomainResult<Sign, SignRetrieveError> =
        signRepository.findFirstByUserIdOrderByUploadAtDesc(userId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(SignNotFound)
}