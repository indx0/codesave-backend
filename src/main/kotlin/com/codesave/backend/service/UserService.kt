package com.codesave.backend.service

import com.codesave.backend.dto.user.*

interface UserService {
    fun register(dto: UserRegisterRequest): UserResponse
    fun login(dto: LoginRequest): TokenResponse
    fun getUserByEmail(email: String): UserResponse
    fun refresh(dto: RefreshRequest): TokenResponse
    fun logout(dto: LogoutRequest)
}