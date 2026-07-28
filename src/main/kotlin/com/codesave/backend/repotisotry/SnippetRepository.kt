package com.codesave.backend.repotisotry

import com.codesave.backend.entity.Snippet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface SnippetRepository : JpaRepository<Snippet, UUID> {
    fun findAllByUserEmail(email: String, pageable: Pageable): Page<Snippet>
    fun findByIdAndUserEmail(id: UUID, userEmail: String): Optional<Snippet>

    @Query("""
        SELECT DISTINCT s FROM Snippet s
        WHERE s.user.id = :userId
        AND (
            LOWER(s.name) LIKE :query
            OR LOWER(s.description) LIKE :query
            OR LOWER(s.code) LIKE :query
            OR EXISTS (SELECT t FROM s.tags t WHERE LOWER(t.name) LIKE :query)
        )
    """)
    fun globalSearch(
        @Param("userId") userId: UUID,
        @Param("query") query: String,
        pageable: Pageable
    ): Page<Snippet>
}