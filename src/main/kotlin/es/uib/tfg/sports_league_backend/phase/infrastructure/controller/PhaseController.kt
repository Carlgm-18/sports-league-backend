package es.uib.tfg.sports_league_backend.phase.infrastructure.controller

import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.phase.application.PhaseService
import es.uib.tfg.sports_league_backend.phase.domain.errors.PhaseNotFound
import es.uib.tfg.sports_league_backend.phase.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sportsapi.dto.PhaseCreateRequest
import es.uib.tfg.sportsapi.dto.PhaseUpdateRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/leagues/{leagueId}/phases")
class PhaseController(
    private val phaseService: PhaseService
) {

    @GetMapping
    fun getLeaguePhases(@PathVariable leagueId: Long): ResponseEntity<*> {
        val phases = phaseService.findAllByLeagueId(leagueId)
        return ResponseEntity.ok(phases.map { it.toDetailsDTO() })
    }

    @PostMapping
    @PreAuthorize("@leagueSecurityGuard.isAdmin(principal, #leagueId)")
    fun createPhase(
        @PathVariable leagueId: Long,
        @Valid @RequestBody request: PhaseCreateRequest
    ): ResponseEntity<*> {
        return when (val result = phaseService.createPhase(leagueId, request)) {
            is DomainResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.data.toDetailsDTO())

            is DomainResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "league"
                        )
                    )
        }
    }

    @GetMapping("/{phaseId}")
    fun getPhaseDetails(
        @PathVariable leagueId: Long,
        @PathVariable phaseId: Long
    ): ResponseEntity<*> {
        return when (val result = phaseService.findById(phaseId)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDetailsDTO())
            is DomainResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "phase"
                        )
                    )
        }
    }

    @PatchMapping("/{phaseId}")
    @PreAuthorize("@leagueSecurityGuard.isAdmin(principal, #leagueId)")
    fun updatePhase(
        @PathVariable leagueId: Long,
        @PathVariable phaseId: Long,
        @Valid @RequestBody request: PhaseUpdateRequest
    ): ResponseEntity<*> {
        return when (val result = phaseService.updatePhase(phaseId, request)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDetailsDTO())
            is DomainResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "phase"
                        )
                    )
        }
    }
}