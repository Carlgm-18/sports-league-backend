package es.uib.tfg.sports_league_backend.common.exception

import es.uib.tfg.sportsapi.dto.FormErrorResponse
import es.uib.tfg.sportsapi.dto.ValidationError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<FormErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { fieldError ->
            ValidationError(
                field = fieldError.field,
                message = fieldError.defaultMessage ?: "Valor no válido",
                rejectedValue = fieldError.rejectedValue?.toString() ?: ""
            )
        }

        val response = FormErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            errors = errors
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
}
