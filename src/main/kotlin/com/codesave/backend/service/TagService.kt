package com.codesave.backend.service

import com.codesave.backend.dto.snippet.SnippetResponse
import com.codesave.backend.dto.tag.TagCreateRequest
import com.codesave.backend.dto.tag.TagResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface TagService {
    fun createTag(dto: TagCreateRequest, userEmail: String): TagResponse
    fun deleteTag(tagId: UUID, userEmail: String)
    fun getAllTags(userEmail: String, pageable: Pageable): Page<TagResponse>
    fun getTagById(tagId: UUID, userEmail: String): TagResponse
    fun getAllSnippetsByTagId(tagId: UUID, userEmail: String): List<SnippetResponse>
}