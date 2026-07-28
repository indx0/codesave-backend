package com.codesave.backend.service

import com.codesave.backend.dto.user.LoginRequest
import com.codesave.backend.dto.user.LogoutRequest
import com.codesave.backend.dto.user.RefreshRequest
import com.codesave.backend.dto.user.UserRegisterRequest
import com.codesave.backend.entity.RefreshToken
import com.codesave.backend.entity.User
import com.codesave.backend.exception.UserExistsException
import com.codesave.backend.repotisotry.RefreshTokenRepository
import com.codesave.backend.repotisotry.UserRepository
import com.codesave.backend.security.JwtTokenProvider
import com.codesave.backend.service.impl.UserServiceImpl
import org.codesave.backend.exception.InvalidRefreshTokenException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Mock
    private lateinit var refreshTokenProvider: RefreshTokenProvider

    @Mock
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var userService: UserServiceImpl

    @BeforeEach
    fun setUp() {
        userService = UserServiceImpl(
            userRepository, refreshTokenRepository,
            refreshTokenProvider, jwtTokenProvider, passwordEncoder
        )
    }

    // ---------- register ----------

    @Test
    fun `register persists new user when email not taken`() {
        val dto = UserRegisterRequest(
            email = "new@example.com",
            name = "New User",
            password = "plaintext-pw"
        )

        whenever(userRepository.existsByEmail("new@example.com")).thenReturn(false)
        whenever(passwordEncoder.encode(any<CharSequence>())).thenReturn("encoded-password")
        whenever(userRepository.save(any<User>())).thenAnswer { inv ->
            val u = inv.getArgument<User>(0)
            u.id = UUID.randomUUID()
            u
        }

        val result = userService.register(dto)

        val captor = argumentCaptor<User>()
        verify(userRepository).save(captor.capture())
        val persisted = captor.firstValue
        assert(persisted.email == "new@example.com")
        assert(persisted.name == "New User")
        assert(persisted.passwordHash == "encoded-password")
        assert(persisted.passwordHash != "plaintext-pw")
        assert(result.email == "new@example.com")
    }

    @Test
    fun `register throws when user already exists`() {
        val dto = UserRegisterRequest(
            email = "taken@example.com",
            name = "Someone",
            password = "pw"
        )

        whenever(userRepository.existsByEmail("taken@example.com")).thenReturn(true)

        assertThrows<UserExistsException> {
            userService.register(dto)
        }
        verify(userRepository, never()).save(any())
    }

    // ---------- login ----------

    @Test
    fun `login returns tokens when credentials valid`() {
        val rawPassword = "correct-password"
        val user = User(email = "user@example.com", passwordHash = "encoded-password")
            .also { it.id = UUID.randomUUID() }

        val dto = LoginRequest(email = "user@example.com", password = rawPassword)

        whenever(userRepository.findByEmail("user@example.com")).thenReturn(user)
        whenever(passwordEncoder.matches(rawPassword, "encoded-password")).thenReturn(true)
        whenever(refreshTokenProvider.generateRefreshToken()).thenReturn("raw-refresh-token")
        whenever(refreshTokenProvider.hashToken("raw-refresh-token")).thenReturn("hashed-refresh-token")
        whenever(jwtTokenProvider.generateAccessToken(user)).thenReturn("access-token")
        whenever(refreshTokenRepository.save(any<RefreshToken>())).thenAnswer { inv ->
            val t = inv.getArgument<RefreshToken>(0)
            t.id = UUID.randomUUID(); t
        }

        val result = userService.login(dto)

        assert(result.accessToken == "access-token")
        assert(result.refreshToken == "raw-refresh-token")
        verify(refreshTokenRepository).save(any())
    }

    @Test
    fun `login throws when user not found`() {
        val dto = LoginRequest(email = "missing@example.com", password = "whatever")

        whenever(userRepository.findByEmail("missing@example.com")).thenReturn(null)

        assertThrows<SecurityException> {
            userService.login(dto)
        }
        verify(refreshTokenRepository, never()).save(any())
    }

    @Test
    fun `login throws when password wrong`() {
        val user = User(email = "user@example.com", passwordHash = "encoded-real-pw")
            .also { it.id = UUID.randomUUID() }
        val dto = LoginRequest(email = "user@example.com", password = "wrong-password")

        whenever(userRepository.findByEmail("user@example.com")).thenReturn(user)
        whenever(passwordEncoder.matches("wrong-password", "encoded-real-pw")).thenReturn(false)

        assertThrows<SecurityException> {
            userService.login(dto)
        }
        verify(refreshTokenRepository, never()).save(any())
    }

    // ---------- getUserByEmail ----------

    @Test
    fun `getUserByEmail returns user when found`() {
        val user = User(email = "found@example.com", name = "Found")
            .also { it.id = UUID.randomUUID() }

        whenever(userRepository.findByEmail("found@example.com")).thenReturn(user)

        val result = userService.getUserByEmail("found@example.com")

        assert(result.email == "found@example.com")
        assert(result.name == "Found")
    }

    @Test
    fun `getUserByEmail throws not found when missing`() {
        whenever(userRepository.findByEmail("missing@example.com")).thenReturn(null)

        assertThrows<UsernameNotFoundException> {
            userService.getUserByEmail("missing@example.com")
        }
    }

    // ---------- refresh ----------

    @Test
    fun `refresh rotates token when valid`() {
        val user = User(email = "user@example.com").also { it.id = UUID.randomUUID() }

        val existing = RefreshToken(
            tokenHash = "old-hashed-token",
            user = user,
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            createdAt = Instant.now(),
            revoked = false
        ).also { it.id = UUID.randomUUID() }

        val dto = RefreshRequest(refreshToken = "old-raw-token")

        whenever(refreshTokenProvider.hashToken("old-raw-token")).thenReturn("old-hashed-token")
        whenever(refreshTokenRepository.findByTokenHash("old-hashed-token")).thenReturn(existing)
        whenever(refreshTokenProvider.generateRefreshToken()).thenReturn("new-raw-token")
        whenever(refreshTokenProvider.hashToken("new-raw-token")).thenReturn("new-hashed-token")
        whenever(jwtTokenProvider.generateAccessToken(user)).thenReturn("new-access-token")
        whenever(refreshTokenRepository.save(any<RefreshToken>())).thenAnswer { inv ->
            val t = inv.getArgument<RefreshToken>(0)
            t.id = UUID.randomUUID(); t
        }

        val result = userService.refresh(dto)

        assert(result.accessToken == "new-access-token")
        assert(result.refreshToken == "new-raw-token")
        assert(existing.revoked) { "old token should be marked revoked" }

        val captor = argumentCaptor<RefreshToken>()
        verify(refreshTokenRepository).save(captor.capture())
        assert(captor.firstValue.tokenHash == "new-hashed-token")
        assert(!captor.firstValue.revoked)
    }

    @Test
    fun `refresh throws when token not found`() {
        val dto = RefreshRequest(refreshToken = "unknown-token")

        whenever(refreshTokenProvider.hashToken("unknown-token")).thenReturn("unknown-hash")
        whenever(refreshTokenRepository.findByTokenHash("unknown-hash")).thenReturn(null)

        assertThrows<InvalidRefreshTokenException> {
            userService.refresh(dto)
        }
    }

    @Test
    fun `refresh throws when token already revoked`() {
        val existing = RefreshToken(
            tokenHash = "reused-hash", user = User(),
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            createdAt = Instant.now(), revoked = true
        ).also { it.id = UUID.randomUUID() }

        val dto = RefreshRequest(refreshToken = "reused-token")

        whenever(refreshTokenProvider.hashToken("reused-token")).thenReturn("reused-hash")
        whenever(refreshTokenRepository.findByTokenHash("reused-hash")).thenReturn(existing)

        assertThrows<InvalidRefreshTokenException> {
            userService.refresh(dto)
        }
    }

    @Test
    fun `refresh throws when token expired`() {
        val existing = RefreshToken(
            tokenHash = "expired-hash", user = User(),
            expiresAt = Instant.now().minus(1, ChronoUnit.DAYS),
            createdAt = Instant.now(), revoked = false
        ).also { it.id = UUID.randomUUID() }

        val dto = RefreshRequest(refreshToken = "expired-token")

        whenever(refreshTokenProvider.hashToken("expired-token")).thenReturn("expired-hash")
        whenever(refreshTokenRepository.findByTokenHash("expired-hash")).thenReturn(existing)

        assertThrows<InvalidRefreshTokenException> {
            userService.refresh(dto)
        }
    }

    // ---------- logout ----------

    @Test
    fun `logout deletes token when valid`() {
        val existing = RefreshToken(
            tokenHash = "valid-hash", user = User(),
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            createdAt = Instant.now(), revoked = false
        ).also { it.id = UUID.randomUUID() }

        val dto = LogoutRequest(refreshToken = "valid-token")

        whenever(refreshTokenProvider.hashToken("valid-token")).thenReturn("valid-hash")
        whenever(refreshTokenRepository.findByTokenHash("valid-hash")).thenReturn(existing)

        userService.logout(dto)

        verify(refreshTokenRepository).delete(existing)
    }

    @Test
    fun `logout throws when token not found`() {
        val dto = LogoutRequest(refreshToken = "unknown-token")

        whenever(refreshTokenProvider.hashToken("unknown-token")).thenReturn("unknown-hash")
        whenever(refreshTokenRepository.findByTokenHash("unknown-hash")).thenReturn(null)

        assertThrows<InvalidRefreshTokenException> {
            userService.logout(dto)
        }
        verify(refreshTokenRepository, never()).delete(any())
    }

    @Test
    fun `logout throws when token already revoked`() {
        val existing = RefreshToken(
            tokenHash = "revoked-hash", user = User(),
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            createdAt = Instant.now(), revoked = true
        ).also { it.id = UUID.randomUUID() }

        val dto = LogoutRequest(refreshToken = "revoked-token")

        whenever(refreshTokenProvider.hashToken("revoked-token")).thenReturn("revoked-hash")
        whenever(refreshTokenRepository.findByTokenHash("revoked-hash")).thenReturn(existing)

        assertThrows<InvalidRefreshTokenException> {
            userService.logout(dto)
        }
        verify(refreshTokenRepository, never()).delete(any())
    }

    @Test
    fun `logout throws when token expired`() {
        val existing = RefreshToken(
            tokenHash = "expired-hash", user = User(),
            expiresAt = Instant.now().minus(1, ChronoUnit.DAYS),
            createdAt = Instant.now(), revoked = false
        ).also { it.id = UUID.randomUUID() }

        val dto = LogoutRequest(refreshToken = "expired-token")

        whenever(refreshTokenProvider.hashToken("expired-token")).thenReturn("expired-hash")
        whenever(refreshTokenRepository.findByTokenHash("expired-hash")).thenReturn(existing)

        assertThrows<InvalidRefreshTokenException> {
            userService.logout(dto)
        }
        verify(refreshTokenRepository, never()).delete(any())
    }
}
