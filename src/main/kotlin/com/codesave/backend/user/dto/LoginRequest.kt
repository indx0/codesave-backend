package com.codesave.backend.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Email
    @field:NotBlank
    val email: String? = null,
    @field:NotBlank
    val password: String? = null,
)
