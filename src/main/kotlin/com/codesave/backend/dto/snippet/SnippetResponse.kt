package com.codesave.backend.dto.snippet

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.*

data class SnippetResponse(
    val id: UUID,
    val name: String,
    val description: String,
    val language: String,
    val code: String,
    @JsonProperty("public")
    val isPublic: Boolean = false,
    val createdAt: Instant,
    val tagNames: List<String> = emptyList()
)