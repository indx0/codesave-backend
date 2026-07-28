package com.codesave.backend.service.impl

import com.codesave.backend.dto.snippet.SnippetResponse
import com.codesave.backend.dto.tag.TagCreateRequest
import com.codesave.backend.dto.tag.TagResponse
import com.codesave.backend.entity.Snippet
import com.codesave.backend.entity.Tag
import com.codesave.backend.exception.TagNotFoundException
import com.codesave.backend.repotisotry.TagRepository
import com.codesave.backend.repotisotry.UserRepository
import com.codesave.backend.service.TagService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class TagServiceImpl(
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository
) : TagService {

    override fun createTag(dto: TagCreateRequest, userEmail: String): TagResponse {
        val user = userRepository.findByEmail(userEmail)!!
        val tag = Tag(
            name = dto.name!!,
            user = user
        )
        tagRepository.save(tag)
        return tag.toResponse()
    }

    override fun deleteTag(tagId: UUID, userEmail: String) {
        val tag = tagRepository.findById(tagId).orElseThrow { TagNotFoundException("Tag not found") }
        if (tag.user.email != userEmail) {
            throw TagNotFoundException("Tag not found")
        }
        tagRepository.delete(tag)
    }

    @Transactional(readOnly = true)
    override fun getAllTags(userEmail: String, pageable: Pageable): Page<TagResponse> {
        return tagRepository.findAllByUserEmail(userEmail, pageable)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    override fun getTagById(tagId: UUID, userEmail: String): TagResponse {
        val tag = tagRepository.findByIdAndUserEmail(tagId, userEmail)
            .orElseThrow { TagNotFoundException("Tag not found") }
        return tag.toResponse()
    }

    @Transactional(readOnly = true)
    override fun getAllSnippetsByTagId(tagId: UUID, userEmail: String): List<SnippetResponse> {
        val tag = tagRepository.findByIdAndUserEmail(tagId, userEmail)
            .orElseThrow { TagNotFoundException("Tag not found") }
        return tag.snippets.map { it.toSnippetResponse() }
    }

    private fun Tag.toResponse() = TagResponse(
        id = id!!,
        name = name
    )

    private fun Snippet.toSnippetResponse() = SnippetResponse(
        id = id!!,
        name = name,
        description = description,
        language = language,
        code = code,
        isPublic = isPublic,
        createdAt = createdAt!!,
        tagNames = tags.map { it.name }
    )
}