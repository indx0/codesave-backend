package com.codesave.backend.repotisotry

import com.codesave.backend.entity.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface TagRepository: JpaRepository<Tag, UUID> {
    fun findByIdAndUserEmail(id: UUID, userEmail: String): Optional<Tag>
    fun findAllByUserEmail(userEmail: String, pageable: Pageable): Page<Tag>
    fun findByIdInAndUserEmail(ids: List<UUID>, userEmail: String): List<Tag>
}