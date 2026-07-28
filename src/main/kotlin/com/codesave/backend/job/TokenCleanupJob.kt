package com.codesave.backend.job

import com.codesave.backend.repotisotry.RefreshTokenRepository
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class TokenCleanupJob(private final val refreshTokenRepository: RefreshTokenRepository) {
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun cleanupTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now(), Instant.now().minus(30, ChronoUnit.DAYS))
    }
}