package com.codesave.backend.dto.tag

import jakarta.validation.constraints.NotBlank

data class TagCreateRequest (
    @field:NotBlank(message = "Name cannot be blank")
    val name: String? = null
)