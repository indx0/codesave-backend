package com.codesave.backend.dto.user

import jakarta.validation.constraints.NotBlank

data class RefreshRequest (
    @field:NotBlank
    val refreshToken: String? = null
)