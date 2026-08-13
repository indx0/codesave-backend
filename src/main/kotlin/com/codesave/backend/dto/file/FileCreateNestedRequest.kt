package com.codesave.backend.dto.file

import jakarta.validation.constraints.NotBlank

class FileCreateNestedRequest {
    @field:NotBlank(message = "Filename cannot be blank")
    var filename: String? = null

    @field:NotBlank(message = "Code cannot be blank")
    var code: String? = null
}
