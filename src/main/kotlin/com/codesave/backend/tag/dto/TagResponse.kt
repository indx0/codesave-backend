package com.codesave.backend.tag.dto

import java.util.UUID

data class TagResponse(
    val id: UUID,
    val name: String,
)
