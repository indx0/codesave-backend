package com.codesave.backend.exception.handler

import com.codesave.backend.exception.SnippetNotFoundException
import com.codesave.backend.exception.TagNotFoundException
import com.codesave.backend.exception.UserExistsException
import org.codesave.backend.exception.InvalidRefreshTokenException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserExistsException::class)
    fun handleUserExistsException(e: UserExistsException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(TagNotFoundException::class)
    fun handleTagNotFoundException(e: TagNotFoundException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(SnippetNotFoundException::class)
    fun handleSnippetNotFoundException(e: SnippetNotFoundException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(InvalidRefreshTokenException::class)
    fun handleInvalidRefreshTokenException(e: InvalidRefreshTokenException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.UNAUTHORIZED)
    }

}