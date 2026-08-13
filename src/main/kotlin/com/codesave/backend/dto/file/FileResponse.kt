package com.codesave.backend.dto.file

import java.util.UUID

data class FileResponse(
    val id: UUID?,
    val filename: String,
    val code: String,
)
