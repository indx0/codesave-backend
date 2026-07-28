package com.codesave.backend.dto.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRegisterRequest (
    @field:NotBlank(message = "Name cannot be blank")
    val name: String? = null,

    @field:Email(message = "Invalid email")
    @field:NotBlank(message = "Email cannot be blank")
    val email: String? = null,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 8, message = "Password must be at least 8 symbols")
    val password: String? = null
)