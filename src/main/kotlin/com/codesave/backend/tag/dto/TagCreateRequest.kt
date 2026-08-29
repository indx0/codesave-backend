package com.codesave.backend.tag.dto

import jakarta.validation.constraints.NotBlank

data class TagCreateRequest(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String? = null,
)
