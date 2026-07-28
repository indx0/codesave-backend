package com.codesave.backend.service.impl

import com.codesave.backend.dto.snippet.SnippetCreateRequest
import com.codesave.backend.dto.snippet.SnippetResponse
import com.codesave.backend.entity.Snippet
import com.codesave.backend.exception.SnippetNotFoundException
import com.codesave.backend.repotisotry.SnippetRepository
import com.codesave.backend.repotisotry.TagRepository
import com.codesave.backend.repotisotry.UserRepository
import com.codesave.backend.service.SnippetService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class SnippetServiceImpl(
    private val snippetRepository: SnippetRepository,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository
) : SnippetService {

    @Transactional(readOnly = true)
    override fun getAllSnippets(userEmail: String, pageable: Pageable): Page<SnippetResponse> {
        return snippetRepository.findAllByUserEmail(userEmail, pageable)
            .map { it.toResponse() }
    }

    override fun createSnippet(dto: SnippetCreateRequest, userEmail: String): SnippetResponse {
        val user = userRepository.findByEmail(userEmail)!!
        val snippet = Snippet(
            name = dto.name!!,
            description = dto.description!!,
            language = dto.language!!,
            code = dto.code!!,
            isPublic = dto.isPublic ?: false,
            user = user
        ).apply {
            if (!dto.tagIds.isNullOrEmpty()) {
                tags = tagRepository.findByIdInAndUserEmail(dto.tagIds, userEmail).toMutableList()
            }
        }
        snippetRepository.save(snippet)
        return snippet.toResponse()
    }

    @Transactional(readOnly = true)
    override fun getSnippetById(snippetId: String, userEmail: String): SnippetResponse {
        val snippet = snippetRepository.findByIdAndUserEmail(UUID.fromString(snippetId), userEmail)
            .orElseThrow { SnippetNotFoundException("Snippet not found") }
        return snippet.toResponse()
    }

    @Transactional(readOnly = true)
    override fun getPublic(snippetId: UUID): SnippetResponse {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { SnippetNotFoundException("Snippet not found") }
        if (!snippet.isPublic) {
            throw SnippetNotFoundException("Snippet not found")
        }
        return snippet.toResponse()
    }

    @Transactional(readOnly = true)
    override fun searchSnippets(query: String?, userEmail: String, pageable: Pageable): Page<SnippetResponse> {
        if (query.isNullOrBlank()) {
            return getAllSnippets(userEmail, pageable)
        }
        val user = userRepository.findByEmail(userEmail)!!
        return snippetRepository.globalSearch(user.id!!, "%${query.lowercase()}%", pageable)
            .map { it.toResponse() }
    }

    override fun updateSnippet(dto: SnippetCreateRequest, userEmail: String, snippetId: UUID): SnippetResponse {
        val snippet = snippetRepository.findByIdAndUserEmail(snippetId, userEmail)
            .orElseThrow { SnippetNotFoundException("Snippet not found") }

        dto.name?.let { snippet.name = it }
        dto.description?.let { snippet.description = it }
        dto.code?.let { snippet.code = it }
        dto.language?.let { snippet.language = it }
        dto.isPublic?.let { snippet.isPublic = it }
        if (dto.tagIds != null) {
            val tags = tagRepository.findByIdInAndUserEmail(dto.tagIds, userEmail)
            if (tags.size != dto.tagIds.size) {
                throw IllegalArgumentException("One or more tags not found")
            }
            snippet.tags = tags.toMutableList()
        }

        return snippet.toResponse()
    }

    override fun deleteSnippet(snippetId: String, userEmail: String) {
        val snippet = snippetRepository.findByIdAndUserEmail(UUID.fromString(snippetId), userEmail)
            .orElseThrow { SnippetNotFoundException("Snippet not found") }
        snippetRepository.delete(snippet)
    }

    private fun Snippet.toResponse() = SnippetResponse(
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