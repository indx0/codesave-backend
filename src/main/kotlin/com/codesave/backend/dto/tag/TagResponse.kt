package com.codesave.backend.dto.tag

import java.util.UUID

data class TagResponse(
    val id: UUID,
    val name: String,
)
