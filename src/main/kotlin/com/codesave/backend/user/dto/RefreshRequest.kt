package com.codesave.backend.user.dto

import jakarta.validation.constraints.NotBlank

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String? = null,
)
