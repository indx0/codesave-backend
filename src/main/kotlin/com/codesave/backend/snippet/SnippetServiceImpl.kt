package com.codesave.backend.snippet

import com.codesave.backend.snippet.dto.FileResponse
import com.codesave.backend.snippet.dto.SnippetCreateRequest
import com.codesave.backend.snippet.dto.SnippetResponse
import com.codesave.backend.tag.TagRepository
import com.codesave.backend.user.UserRepository
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
    private val userRepository: UserRepository,
) : SnippetService {
    @Transactional(readOnly = true)
    override fun getAllSnippets(
        userEmail: String,
        pageable: Pageable,
    ): Page<SnippetResponse> =
        snippetRepository
            .findAllByUserEmail(userEmail, pageable)
            .map { it.toResponse() }

    override fun createSnippet(
        dto: SnippetCreateRequest,
        userEmail: String,
    ): SnippetResponse {
        val user = userRepository.findByEmail(userEmail)!!
        val snippet =
            Snippet(
                name = dto.name!!,
                description = dto.description!!,
                isPublic = dto.isPublic ?: false,
                user = user,
            ).apply {
                if (!dto.tagIds.isNullOrEmpty()) {
                    tags = tagRepository.findByIdInAndUserEmail(dto.tagIds, userEmail).toMutableList()
                }
                if (!dto.files.isNullOrEmpty()) {
                    dto.files.forEach { file ->
                        files.add(SnippetFile(snippet = this, filename = file.filename!!, code = file.code!!))
                    }
                }
            }
        snippetRepository.save(snippet)
        return snippet.toResponse()
    }

    @Transactional(readOnly = true)
    override fun getSnippetById(
        snippetId: String,
        userEmail: String,
    ): SnippetResponse {
        val snippet =
            snippetRepository
                .findByIdAndUserEmail(UUID.fromString(snippetId), userEmail)
                .orElseThrow { SnippetNotFoundException("Snippet not found") }
        return snippet.toResponse()
    }

    @Transactional(readOnly = true)
    override fun getPublic(snippetId: UUID): SnippetResponse {
        val snippet =
            snippetRepository
                .findById(snippetId)
                .orElseThrow { SnippetNotFoundException("Snippet not found") }
        if (!snippet.isPublic) {
            throw SnippetNotFoundException("Snippet not found")
        }
        return snippet.toResponse()
    }

    @Transactional(readOnly = true)
    override fun searchSnippets(
        query: String?,
        userEmail: String,
        pageable: Pageable,
    ): Page<SnippetResponse> {
        if (query.isNullOrBlank()) {
            return getAllSnippets(userEmail, pageable)
        }
        val user = userRepository.findByEmail(userEmail)!!
        return snippetRepository
            .globalSearch(user.id!!, "%${query.lowercase()}%", pageable)
            .map { it.toResponse() }
    }

    override fun updateSnippet(
        dto: SnippetCreateRequest,
        userEmail: String,
        snippetId: UUID,
    ): SnippetResponse {
        val snippet =
            snippetRepository
                .findByIdAndUserEmail(snippetId, userEmail)
                .orElseThrow { SnippetNotFoundException("Snippet not found") }

        dto.name?.let { snippet.name = it }
        dto.description?.let { snippet.description = it }
        dto.isPublic?.let { snippet.isPublic = it }
        if (dto.tagIds != null) {
            val tags = tagRepository.findByIdInAndUserEmail(dto.tagIds, userEmail)
            if (tags.size != dto.tagIds.size) {
                throw IllegalArgumentException("One or more tags not found")
            }
            snippet.tags = tags.toMutableList()
        }
        if(dto.files != null) {
            snippet.files.clear()
            dto.files.forEach { file ->
                snippet.files.add(SnippetFile(snippet = snippet, filename = file.filename!!, code = file.code!!))
            }
        }

        return snippet.toResponse()
    }

    override fun deleteSnippet(
        snippetId: String,
        userEmail: String,
    ) {
        val snippet =
            snippetRepository
                .findByIdAndUserEmail(UUID.fromString(snippetId), userEmail)
                .orElseThrow { SnippetNotFoundException("Snippet not found") }
        snippetRepository.delete(snippet)
    }

    private fun Snippet.toResponse() =
        SnippetResponse(
            id = id!!,
            name = name,
            description = description,
            isPublic = isPublic,
            createdAt = createdAt!!,
            tagNames = tags.map { it.name },
            files =
                files.map {
                    FileResponse(
                        id = it.id,
                        filename = it.filename,
                        code = it.code,
                    )
                },
        )
}
