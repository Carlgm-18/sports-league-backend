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


@RestController
@RequestMapping("/api/v1")
class RequestController(
    private val requestService: RequestService
) {

    @PostMapping("/leagues/{leagueId}/requests")
    fun createRequest(
        @PathVariable leagueId: Long,
        @Valid @RequestBody request: BaseRequest
    ): ResponseEntity<*> {

        val result = when(request) {
            is RefereeRequest -> requestService.createRefereeRequest(request)
            is TeamCreateRequestDTO -> requestService.createTeamCreateRequest(request)
            is TeamJoinRequestDTO -> requestService.createTeamJoinRequest(request)
            else -> throw IllegalArgumentException("Unexpected type of request")
        }

        return when (result) {
            is DomainResult.Success ->
                ResponseEntity.status(HttpStatus.CREATED).body(result.data.toDTO())
            is DomainResult.Failure ->
                mapError(result.error)
        }
    }

    @PatchMapping("/requests/{requestId}")
    fun resolveRequest(
        @PathVariable requestId: Long,
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody input: ResolveRequestInput
    ): ResponseEntity<*> {

        return when (val result = requestService.resolveRequest(requestId, input, userId)) {
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

    @GetMapping("/requests/{requestId}")
    fun getRequest(
        @PathVariable requestId: Long,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<*> {
        return when (val result = requestService.findRequestByRequestId(requestId, userId)) {
            is DomainResult.Failure ->
                mapError(result.error)

            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDTO())
        }
    }

    private fun mapError(error: CreateRequestError): ResponseEntity<*> =
        when (error) {
            is ParticipantAndTeamLeagueMissmatch ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                    mapOf(
                        "error" to ErrorCode.SUBRESOURCE_MISMATCH,
                        "subresource" to "league",
                        "resources" to listOf("team", "participant")
                    )
                )
            is ParticipantNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "error" to ErrorCode.RESOURCE_NOT_FOUND,
                        "resource" to "participant"
                    )
                )
            is TeamNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "error" to ErrorCode.RESOURCE_NOT_FOUND,
                        "resource" to "team"
                    )
                )

            LeagueNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "error" to ErrorCode.RESOURCE_NOT_FOUND,
                        "resource" to "league"
                    )
                )
        }

    private fun mapError(error: ResolveRequestError): ResponseEntity<*> =
        when (error) {
            CouldNotCreateTeam ->
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        mapOf(
                            "error" to ErrorCode.CREATE_RESOURCE_ERROR,
                            "resource" to "team"
                        )
                    )
            InvalidRequestState ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.REQUEST_ALREADY_RESOLVED,
                        )
                    )
            LeagueNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "error" to ErrorCode.RESOURCE_NOT_FOUND,
                        "resource" to "team"
                    )
                )
            ParticipantNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "error" to ErrorCode.RESOURCE_NOT_FOUND,
                        "resource" to "participant"
                    )
                )
            RequestNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "error" to ErrorCode.RESOURCE_NOT_FOUND,
                        "resource" to "request"
                    )
                )
            TeamNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "error" to ErrorCode.RESOURCE_NOT_FOUND,
                        "resource" to "team"
                    )
                )
            UnauthorizedAction ->
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    mapOf(
                        "error" to ErrorCode.UNAUTHORIZED_ERROR
                    )
                )
        }

    private fun mapError(error: RetrieveRequestError): ResponseEntity<*> =
        when (error) {
            RequestNotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "request"
                        )
                    )

            ParticipantNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "error" to ErrorCode.RESOURCE_NOT_FOUND,
                        "resource" to "participant"
                    )
                )

            TeamNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "error" to ErrorCode.RESOURCE_NOT_FOUND,
                        "resource" to "team"
                    )
                )

            UnauthorizedAction ->
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    mapOf(
                        "error" to ErrorCode.UNAUTHORIZED_ERROR
                    )
                )
        }
}
