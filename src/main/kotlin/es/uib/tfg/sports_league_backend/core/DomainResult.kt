package es.uib.tfg.sports_league_backend.core

sealed class DomainResult<out S, out E> {
    data class Success<out S>(val data: S) : DomainResult<S, Nothing>()
    data class Failure<out E>(val error: E) : DomainResult<Nothing, E>()
}