package es.uib.tfg.sports_league_backend.participant.infrastructure.controller;

import es.uib.tfg.sports_league_backend.participant.application.ParticipantService
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leagues/{leagueId}/participants")
class ParticipantController(
    private val participantService: ParticipantService
) {


}
