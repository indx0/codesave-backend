package com.codesave.backend.service

import com.codesave.backend.dto.snippet.SnippetCreateRequest
import com.codesave.backend.dto.snippet.SnippetResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface SnippetService {
    fun getAllSnippets(userEmail: String, pageable: Pageable): Page<SnippetResponse>
    fun getSnippetById(snippetId: String, userEmail: String): SnippetResponse
    fun getPublic(snippetId: UUID): SnippetResponse
    fun searchSnippets(query: String?, userEmail: String, pageable: Pageable): Page<SnippetResponse>
    fun createSnippet(dto: SnippetCreateRequest, userEmail: String): SnippetResponse
    fun updateSnippet(dto: SnippetCreateRequest, userEmail: String, snippetId: UUID): SnippetResponse
    fun deleteSnippet(snippetId: String, userEmail: String)
}