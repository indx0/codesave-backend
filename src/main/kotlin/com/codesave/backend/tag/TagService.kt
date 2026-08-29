package com.codesave.backend.tag

import com.codesave.backend.snippet.dto.SnippetResponse
import com.codesave.backend.tag.dto.TagCreateRequest
import com.codesave.backend.tag.dto.TagResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface TagService {
    fun createTag(
        dto: TagCreateRequest,
        userEmail: String,
    ): TagResponse

    fun deleteTag(
        tagId: UUID,
        userEmail: String,
    )

    fun getAllTags(
        userEmail: String,
        pageable: Pageable,
    ): Page<TagResponse>

    fun getTagById(
        tagId: UUID,
        userEmail: String,
    ): TagResponse

    fun getAllSnippetsByTagId(
        tagId: UUID,
        userEmail: String,
    ): List<SnippetResponse>
}
