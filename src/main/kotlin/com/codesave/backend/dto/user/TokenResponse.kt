package com.codesave.backend.dto.user

data class TokenResponse (
    val accessToken: String,
    val refreshToken: String
)
