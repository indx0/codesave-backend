package com.codesave.backend.snippet

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface SnippetFileRepository : JpaRepository<SnippetFile, UUID> {
    fun findAllBySnippetId(snippetId: UUID): List<SnippetFile>

    fun findAllBySnippetId(
        snippetId: UUID,
        pageable: Pageable,
    ): Page<SnippetFile>

    fun findByIdAndSnippetId(
        id: UUID,
        snippetId: UUID,
    ): Optional<SnippetFile>
}
