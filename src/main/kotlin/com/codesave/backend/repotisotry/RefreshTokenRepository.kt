package com.codesave.backend.repotisotry

import com.codesave.backend.entity.RefreshToken
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM RefreshToken r
        WHERE (r.revoked = true AND r.expiresAt < :now)
           OR r.expiresAt < :daysAgo
    """)
    fun deleteExpiredTokens(now: Instant, daysAgo: Instant): Int

    fun findByTokenHash(token: String): RefreshToken?
}