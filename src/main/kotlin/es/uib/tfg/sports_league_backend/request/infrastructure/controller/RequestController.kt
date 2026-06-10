package es.uib.tfg.sports_league_backend.request.infrastructure.controller

import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.request.application.RequestService
import es.uib.tfg.sports_league_backend.request.domain.errors.*
import es.uib.tfg.sports_league_backend.request.infrastructure.mapper.toDTO
import es.uib.tfg.sportsapi.dto.BaseRequest
import es.uib.tfg.sportsapi.dto.RefereeRequest
import es.uib.tfg.sportsapi.dto.ResolveRequestInput
import jakarta.validation.Valid
import es.uib.tfg.sportsapi.dto.TeamCreateRequest as TeamCreateRequestDTO
import es.uib.tfg.sportsapi.dto.TeamJoinRequest as TeamJoinRequestDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

data class ResolveRequestInput(
    val status: RequestState,
    val rejectionReason: String? = null
)

@RestController
@RequestMapping("/api/v1")
class RequestController(
    private val requestService: RequestService
) {

    @PostMapping("/leagues/{leagueId}/referee-requests")
    fun createRefereeRequest(
        @PathVariable leagueId: Long,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<*> =
        when (val result = requestService.createRefereeRequest(leagueId, userId)) {
            is DomainResult.Success ->
                ResponseEntity.status(HttpStatus.CREATED).body(result.data.toDTO())
            is DomainResult.Failure ->
                mapError(result.error)
        }

    @PatchMapping("/leagues/{leagueId}/referee-requests/{requestId}")
    fun resolveRefereeRequest(
        @PathVariable leagueId: Long,
        @PathVariable requestId: Long,
        @AuthenticationPrincipal userId: Long,
        @RequestBody input: ResolveRequestInput
    ): ResponseEntity<*> =
        when (val result = requestService.resolveRefereeRequest(leagueId, requestId, userId, input.status, input.rejectionReason)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDTO())
            is DomainResult.Failure ->
                mapError(result.error)
        }

    @PostMapping("/leagues/{leagueId}/teams")
    fun createTeamRequest(
        @PathVariable leagueId: Long,
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: TeamCreateRequestDTO
    ): ResponseEntity<*> =
        when (val result = requestService.createTeamCreateRequest(
            leagueId = leagueId,
            userId = userId,
            name = request.name,
            initials = request.initials,
            description = request.description,
            motto = request.motto,
            primaryColor = request.primaryColor,
            secondaryColor = request.secondaryColor,
            iconImageUrl = request.iconImageUrl?.toString()
        )) {
            is DomainResult.Success ->
                ResponseEntity.status(HttpStatus.CREATED).body(result.data.toDTO())
            is DomainResult.Failure ->
                mapError(result.error)
        }

    @PatchMapping("/leagues/{leagueId}/team-requests/{requestId}")
    fun resolveTeamCreateRequest(
        @PathVariable leagueId: Long,
        @PathVariable requestId: Long,
        @AuthenticationPrincipal userId: Long,
        @RequestBody input: ResolveRequestInput
    ): ResponseEntity<*> =
        when (val result = requestService.resolveTeamCreateRequest(leagueId, requestId, userId, input.status, input.rejectionReason)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDTO())
            is DomainResult.Failure ->
                mapError(result.error)
        }

    @PostMapping("/teams/{teamId}/join-requests")
    fun createTeamJoinRequest(
        @PathVariable teamId: Long,
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: TeamJoinRequestDTO
    ): ResponseEntity<*> {
        val way = when (request.way) {
            TeamJoinRequestDTO.Way.INVITATION -> TeamJoinWay.INVITATION
            else -> TeamJoinWay.APPLIANCE
        }
        return when (val result = requestService.createTeamJoinRequest(teamId, userId, way)) {
            is DomainResult.Success ->
                ResponseEntity.status(HttpStatus.CREATED).body(result.data.toDTO())
            is DomainResult.Failure ->
                mapError(result.error)
        }
    }

    @GetMapping("/teams/{teamId}/join-requests")
    fun getJoinRequestsByTeamId(
        @PathVariable teamId: Long,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<*> =
        when (val result = requestService.findJoinRequestsByTeamId(teamId, userId)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.map { it.toDTO() })
            is DomainResult.Failure ->
                mapError(result.error)
        }

    @PatchMapping("/teams/{teamId}/join-requests/{requestId}")
    fun resolveTeamJoinRequest(
        @PathVariable teamId: Long,
        @PathVariable requestId: Long,
        @AuthenticationPrincipal userId: Long,
        @RequestBody input: ResolveRequestInput
    ): ResponseEntity<*> =
        when (val result = requestService.resolveTeamJoinRequest(teamId, requestId, userId, input.status)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDTO())
            is DomainResult.Failure ->
                mapError(result.error)
        }

    private fun mapError(error: RequestError): ResponseEntity<*> =
        when (error) {
            is RequestNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf("error" to ErrorCode.RESOURCE_NOT_FOUND, "resource" to "request")
                )
            is LeagueNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf("error" to ErrorCode.RESOURCE_NOT_FOUND, "resource" to "league")
                )
            is ParticipantNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf("error" to ErrorCode.RESOURCE_NOT_FOUND, "resource" to "participant")
                )
            is TeamNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf("error" to ErrorCode.RESOURCE_NOT_FOUND, "resource" to "team")
                )
            is UnauthorizedAction ->
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    mapOf("error" to "UNAUTHORIZED", "message" to "You do not have permissions to perform this action.")
                )
            is InvalidRequestState ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    mapOf("error" to "INVALID_REQUEST_STATE", "message" to "The request is not in PENDING state.")
                )
        }
}
