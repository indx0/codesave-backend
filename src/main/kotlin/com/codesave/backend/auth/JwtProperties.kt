package com.codesave.backend.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val secret: String = "",
    val issuer: String = "",
    val accessTokenExpiration: Long = 0,
    val refreshTokenExpiration: Long = 0,
)
