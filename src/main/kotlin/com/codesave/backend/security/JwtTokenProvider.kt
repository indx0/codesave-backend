package com.codesave.backend.security

import com.codesave.backend.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import kotlin.io.encoding.Base64

@Service
class JwtTokenProvider(private val jwtProperties: JwtProperties) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(Base64.decode(jwtProperties.secret))

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token).payload
    }

    fun generateAccessToken(user: User): String {
        val now = Date()
        val expiry = Date(now.time + jwtProperties.accessTokenExpiration)

        return Jwts.builder()
            .subject(user.id.toString())
            .issuer(jwtProperties.issuer)
            .claim("email", user.email)
            .claim("roles", user.authorities.map { it.authority })
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    fun getEmailFromToken(token: String): String {
        return getClaims(token)["email"] as String
    }

    fun getUserIdFromToken(token: String): UUID {
        return UUID.fromString(getClaims(token).subject)
    }

    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }
}