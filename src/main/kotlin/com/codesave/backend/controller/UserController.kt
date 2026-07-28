package com.codesave.backend.controller

import com.codesave.backend.dto.user.*
import com.codesave.backend.entity.User
import com.codesave.backend.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "User authentication and management")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with email and password")
    @ApiResponse(responseCode = "200", description = "User registered successfully", content = [Content(schema = Schema(implementation = UserResponse::class))])
    @ApiResponse(responseCode = "409", description = "User already exists")
    fun register(@Valid @RequestBody dto: UserRegisterRequest): UserResponse {
        return userService.register(dto)
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and returns JWT access token with refresh token")
    @ApiResponse(responseCode = "200", description = "Login successful", content = [Content(schema = Schema(implementation = TokenResponse::class))])
    @ApiResponse(responseCode = "401", description = "Invalid email or password")
    fun login(@Valid @RequestBody dto: LoginRequest): TokenResponse {
        return userService.login(dto)
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Issues a new access token using a valid refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed", content = [Content(schema = Schema(implementation = TokenResponse::class))])
    @ApiResponse(responseCode = "401", description = "Invalid, expired, or revoked refresh token")
    fun refresh(@Valid @RequestBody dto: RefreshRequest): TokenResponse {
        return userService.refresh(dto)
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes the refresh token")
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    fun logout(@Valid @RequestBody dto: LogoutRequest): ResponseEntity<Unit> {
        userService.logout(dto)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me")
    @Operation(summary = "Current user", description = "Returns the authenticated user's profile")
    @ApiResponse(responseCode = "200", description = "User profile", content = [Content(schema = Schema(implementation = UserResponse::class))])
    fun me(@AuthenticationPrincipal user: User): UserResponse {
        return UserResponse(id = user.id!!, name = user.name, email = user.email)
    }
}