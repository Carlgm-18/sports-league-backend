package es.uib.tfg.sports_league_backend.participant.domain

class ParticipantRole (
    var id: Long? = null,
    var participant: Participant,
    var participationRole: ParticipationRole
)