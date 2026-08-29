package com.codesave.backend.user

import com.codesave.backend.user.dto.LoginRequest
import com.codesave.backend.user.dto.LogoutRequest
import com.codesave.backend.user.dto.RefreshRequest
import com.codesave.backend.user.dto.TokenResponse
import com.codesave.backend.user.dto.UserRegisterRequest
import com.codesave.backend.user.dto.UserResponse

interface UserService {
    fun register(dto: UserRegisterRequest): UserResponse

    fun login(dto: LoginRequest): TokenResponse

    fun getUserByEmail(email: String): UserResponse

    fun refresh(dto: RefreshRequest): TokenResponse

    fun logout(dto: LogoutRequest)
}
