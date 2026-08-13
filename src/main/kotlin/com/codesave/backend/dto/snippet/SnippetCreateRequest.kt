package com.codesave.backend.dto.snippet

import com.codesave.backend.dto.file.FileCreateNestedRequest
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class SnippetCreateRequest(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String? = null,
    @field:NotBlank(message = "Description cannot be blank")
    val description: String? = null,
    @field:Valid
    val files: List<FileCreateNestedRequest>? = null,
    @field:NotNull(message = "isPublic must be specified")
    @field:JsonProperty("public")
    var isPublic: Boolean? = null,
    val tagIds: List<UUID>? = null,
)
