package es.uib.tfg.sports_league_backend.league.domain.errors

sealed interface LeagueCreateError
sealed interface LeagueRetrieveError
sealed interface LeagueJoinError

object SportNotFound : LeagueCreateError
object ConfigurationNotFound : LeagueCreateError
object PunctuationSystemNotFound : LeagueCreateError
object UserNotFound : LeagueCreateError, LeagueJoinError
object LeagueNotFound : LeagueRetrieveError, LeagueJoinError
object InscriptionClosed : LeagueJoinError
object CategoryMismatch : LeagueJoinError
object LeagueAlreadyEnded : LeagueJoinError
object AlreadyJoin: LeagueJoinError

