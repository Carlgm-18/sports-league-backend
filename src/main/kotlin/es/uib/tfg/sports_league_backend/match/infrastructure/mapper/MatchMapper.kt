package es.uib.tfg.sports_league_backend.match.infrastructure.mapper

import es.uib.tfg.sports_league_backend.match.domain.Match
import es.uib.tfg.sports_league_backend.match.domain.Proposal
import es.uib.tfg.sports_league_backend.participant.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sports_league_backend.participant.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.toDomain
import es.uib.tfg.sports_league_backend.availability.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.team.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sports_league_backend.result.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sports_league_backend.result.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sportsapi.dto.MatchSummary
import es.uib.tfg.sportsapi.dto.MatchDetails
import es.uib.tfg.sportsapi.dto.MatchDateProposalDetails

fun Match.toSummaryDTO(): MatchSummary =
    MatchSummary(
        matchId = id!!,
        status = status,
        roundId = roundId,
        localTeam = localTeam?.toSummaryDTO(),
        visitorTeam = visitorTeam?.toSummaryDTO(),
        dateTime = dateTime?.toDetailsDTO(),
        proposalState = if (dateTime == null) MatchSummary.ProposalState.PENDING
        else MatchSummary.ProposalState.PROPOSED,
        firstReferee = firstReferee?.toDomain()?.toSummaryDTO(),
        secondReferee = secondReferee?.toDomain()?.toSummaryDTO(),
        resultSummary = result?.toSummaryDTO(),
    )

fun Match.toDetailsDTO(activeProposal: Proposal?): MatchDetails =
    MatchDetails(
        matchId = id!!,
        status = status,
        proposal = activeProposal?.toDetailsDTO(),
        roundId = roundId,
        localTeam = localTeam?.toSummaryDTO(),
        visitorTeam = visitorTeam?.toSummaryDTO(),
        dateTime = dateTime?.toDetailsDTO(),
        firstReferee = firstReferee?.toDomain()?.toDetailsDTO(),
        secondReferee = secondReferee?.toDomain()?.toDetailsDTO(),
        resultSummary = result?.toSummaryDTO(),
        resultDetails = result?.toDetailsDTO()
    )

fun Proposal.toDetailsDTO(): MatchDateProposalDetails =
    MatchDateProposalDetails(
        proposalId = id!!,
        status = status,
        dateTimeSlot = dateTimeSlot.toDetailsDTO(),
        proposedAt = proposedAt,
        resolvedAt = resolvedAt
    )