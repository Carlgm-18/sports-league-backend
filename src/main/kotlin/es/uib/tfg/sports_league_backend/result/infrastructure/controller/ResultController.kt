package es.uib.tfg.sports_league_backend.result.infrastructure.controller

import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchUpdateError
import es.uib.tfg.sports_league_backend.result.application.ResultService
import es.uib.tfg.sports_league_backend.result.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sportsapi.dto.ResultDetails
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/matches/{matchId}/result")
class ResultController(
    private val resultService: ResultService
) {

    @PutMapping
    @PreAuthorize("@matchSecurityGuard.isFirstReferee(principal, #matchId)")
    fun registerResult(
        @PathVariable matchId: Long,
        @Valid @RequestBody request: ResultDetails
    ): ResponseEntity<*> {
        return when (val result = resultService.saveResult(matchId, request)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDetailsDTO())

            is DomainResult.Failure ->
                mapError(result.error)
        }
    }

    private fun mapError(error: MatchUpdateError): ResponseEntity<*> =
        when (error) {
            MatchNotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "match"
                        )
                    )
            else ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.CREATE_RESOURCE_ERROR
                        )
                    )
        }
}
