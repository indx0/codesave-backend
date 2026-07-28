package com.codesave.backend.dto.snippet

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*

data class SnippetCreateRequest(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String? = null,

    @field:NotBlank(message = "Description cannot be blank")
    val description: String? = null,

    @field:NotBlank(message = "Language cannot be blank")
    val language: String? = null,

    @field:NotBlank(message = "Code cannot be blank")
    val code: String? = null,

    @field:NotNull(message = "isPublic must be specified")
    @field:JsonProperty("public")
    var isPublic: Boolean? = null,

    val tagIds: List<UUID>? = null
)