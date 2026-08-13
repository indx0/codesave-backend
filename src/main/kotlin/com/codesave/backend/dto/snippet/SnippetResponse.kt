package com.codesave.backend.dto.snippet

import com.codesave.backend.dto.file.FileResponse
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

data class SnippetResponse(
    val id: UUID,
    val name: String,
    val description: String,
    val files: List<FileResponse>?,
    @JsonProperty("public")
    val isPublic: Boolean = false,
    val createdAt: Instant,
    val tagNames: List<String> = emptyList(),
)
