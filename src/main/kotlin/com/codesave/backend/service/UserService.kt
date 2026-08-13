package com.codesave.backend.service

import com.codesave.backend.dto.user.LoginRequest
import com.codesave.backend.dto.user.LogoutRequest
import com.codesave.backend.dto.user.RefreshRequest
import com.codesave.backend.dto.user.TokenResponse
import com.codesave.backend.dto.user.UserRegisterRequest
import com.codesave.backend.dto.user.UserResponse

interface UserService {
    fun register(dto: UserRegisterRequest): UserResponse

    fun login(dto: LoginRequest): TokenResponse

    fun getUserByEmail(email: String): UserResponse

    fun refresh(dto: RefreshRequest): TokenResponse

    fun logout(dto: LogoutRequest)
}
