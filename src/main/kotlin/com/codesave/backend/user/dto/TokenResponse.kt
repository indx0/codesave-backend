package com.codesave.backend.user.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
