package es.uib.tfg.sports_league_backend.league.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "league_configuration")
class LeagueConfiguration {
    @Id
    var id: Int = 0
}