package com.codesave.backend.service.impl

import com.codesave.backend.dto.user.*
import com.codesave.backend.entity.RefreshToken
import com.codesave.backend.entity.User
import com.codesave.backend.exception.UserExistsException
import com.codesave.backend.repotisotry.RefreshTokenRepository
import com.codesave.backend.repotisotry.UserRepository
import com.codesave.backend.security.JwtTokenProvider
import com.codesave.backend.service.RefreshTokenProvider
import com.codesave.backend.service.UserService
import org.codesave.backend.exception.InvalidRefreshTokenException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val refreshTokenProvider: RefreshTokenProvider,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    override fun register(dto: UserRegisterRequest): UserResponse {
        if (userRepository.existsByEmail(dto.email!!)) {
            throw UserExistsException("User already exists")
        }
        val user = User(
            email = dto.email,
            name = dto.name!!,
            passwordHash = passwordEncoder.encode(dto.password)
        )
        userRepository.save(user)
        return user.toResponse()
    }

    override fun login(dto: LoginRequest): TokenResponse {
        val user = userRepository.findByEmail(dto.email!!)
            ?: throw SecurityException("Invalid email or password")
        if (!passwordEncoder.matches(dto.password, user.passwordHash)) {
            throw SecurityException("Invalid email or password")
        }

        val rawRefreshToken = refreshTokenProvider.generateRefreshToken()
        val refreshToken = RefreshToken(
            tokenHash = refreshTokenProvider.hashToken(rawRefreshToken),
            user = user,
            expiresAt = Instant.now().plus(14, ChronoUnit.DAYS),
            createdAt = Instant.now(),
            revoked = false
        )
        refreshTokenRepository.save(refreshToken)

        return TokenResponse(
            accessToken = jwtTokenProvider.generateAccessToken(user),
            refreshToken = rawRefreshToken
        )
    }

    override fun getUserByEmail(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found")
        return user.toResponse()
    }

    override fun refresh(dto: RefreshRequest): TokenResponse {
        val hashedToken = refreshTokenProvider.hashToken(dto.refreshToken!!)
        val tokenEntity = refreshTokenRepository.findByTokenHash(hashedToken) ?:
             throw InvalidRefreshTokenException("Invalid refresh token")

        if (tokenEntity.revoked) throw InvalidRefreshTokenException("Refresh token reuse detected")
        if (tokenEntity.expiresAt.isBefore(Instant.now()))
            throw InvalidRefreshTokenException("Refresh token expired")

        tokenEntity.revoked = true
        tokenEntity.revokedAt = Instant.now()
        val user = tokenEntity.user

        val newRawToken = refreshTokenProvider.generateRefreshToken()
        val newToken = RefreshToken(
            tokenHash = refreshTokenProvider.hashToken(newRawToken),
            user = user,
            expiresAt = Instant.now().plus(14, ChronoUnit.DAYS),
            createdAt = Instant.now(),
            revoked = false
        )
        refreshTokenRepository.save(newToken)

        return TokenResponse(
            accessToken = jwtTokenProvider.generateAccessToken(user),
            refreshToken = newRawToken
        )
    }

    override fun logout(dto: LogoutRequest) {
        val hashedToken = refreshTokenProvider.hashToken(dto.refreshToken!!)
        val tokenEntity = refreshTokenRepository.findByTokenHash(hashedToken)
            ?: throw InvalidRefreshTokenException("Invalid refresh token")

        if (tokenEntity.revoked) {
            throw InvalidRefreshTokenException("Refresh token reuse detected")
        }
        if (tokenEntity.expiresAt.isBefore(Instant.now())) {
            throw InvalidRefreshTokenException("Refresh token expired")
        }

        refreshTokenRepository.delete(tokenEntity)
    }

    private fun User.toResponse() = UserResponse(
        id = id!!,
        name = name,
        email = email
    )

}