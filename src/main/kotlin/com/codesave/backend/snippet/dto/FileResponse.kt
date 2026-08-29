package com.codesave.backend.snippet.dto

import java.util.UUID

data class FileResponse(
    val id: UUID?,
    val filename: String,
    val code: String,
)
