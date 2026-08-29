package com.codesave.backend.user

import com.codesave.backend.user.dto.LoginRequest
import com.codesave.backend.user.dto.LogoutRequest
import com.codesave.backend.user.dto.RefreshRequest
import com.codesave.backend.user.dto.TokenResponse
import com.codesave.backend.user.dto.UserRegisterRequest
import com.codesave.backend.user.dto.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "User authentication and management")
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with email and password")
    @ApiResponse(
        responseCode = "200",
        description = "User registered successfully",
        content = [Content(schema = Schema(implementation = UserResponse::class))],
    )
    @ApiResponse(responseCode = "409", description = "User already exists")
    @SecurityRequirements()
    fun register(
        @Valid @RequestBody dto: UserRegisterRequest,
    ): UserResponse = userService.register(dto)

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and returns JWT access token with refresh token")
    @ApiResponse(
        responseCode = "200",
        description = "Login successful",
        content = [Content(schema = Schema(implementation = TokenResponse::class))],
    )
    @ApiResponse(responseCode = "401", description = "Invalid email or password")
    @SecurityRequirements()
    fun login(
        @Valid @RequestBody dto: LoginRequest,
    ): TokenResponse = userService.login(dto)

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Issues a new access token using a valid refresh token")
    @ApiResponse(
        responseCode = "200",
        description = "Token refreshed",
        content = [Content(schema = Schema(implementation = TokenResponse::class))],
    )
    @SecurityRequirements()
    @ApiResponse(responseCode = "401", description = "Invalid, expired, or revoked refresh token")
    fun refresh(
        @Valid @RequestBody dto: RefreshRequest,
    ): TokenResponse = userService.refresh(dto)

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes the refresh token")
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    @SecurityRequirements()
    fun logout(
        @Valid @RequestBody dto: LogoutRequest,
    ): ResponseEntity<Unit> {
        userService.logout(dto)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me")
    @Operation(summary = "Current user", description = "Returns the authenticated user's profile")
    @ApiResponse(
        responseCode = "200",
        description = "User profile",
        content = [Content(schema = Schema(implementation = UserResponse::class))],
    )
    fun me(
        @AuthenticationPrincipal user: User,
    ): UserResponse = UserResponse(id = user.id!!, name = user.name, email = user.email)
}
