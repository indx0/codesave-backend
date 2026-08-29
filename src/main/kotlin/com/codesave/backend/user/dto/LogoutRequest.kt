package com.codesave.backend.user.dto

import jakarta.validation.constraints.NotBlank

data class LogoutRequest(
    @field:NotBlank
    val refreshToken: String? = null,
)
