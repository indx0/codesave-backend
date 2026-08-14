package com.codesave.backend.controller

import com.codesave.backend.dto.snippet.SnippetCreateRequest
import com.codesave.backend.dto.snippet.SnippetResponse
import com.codesave.backend.entity.User
import com.codesave.backend.service.SnippetService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/snippet")
@Tag(name = "Snippet", description = "Snippet CRUD and search operations")
class SnippetController(
    private val snippetService: SnippetService,
) {
    @GetMapping
    @Operation(summary = "Get all snippets", description = "Returns a paginated list of the authenticated user's snippets")
    @ApiResponse(
        responseCode = "200",
        description = "Paginated list of snippets",
        content = [Content(schema = Schema(implementation = SnippetResponse::class))],
    )
    fun getAll(
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = "0") @Parameter page: Int,
        @RequestParam(defaultValue = "20") @Parameter size: Int
    ): Page<SnippetResponse> {
        val p = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        return snippetService.getAllSnippets(user.email, p)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get snippet by ID", description = "Returns a single snippet owned by the authenticated user")
    @ApiResponse(
        responseCode = "200",
        description = "Snippet found",
        content = [Content(schema = Schema(implementation = SnippetResponse::class))],
    )
    @ApiResponse(responseCode = "404", description = "Snippet not found")
    fun getById(
        @AuthenticationPrincipal user: User,
        @PathVariable @Parameter(description = "Snippet UUID (can be with or without hyphens)") id: String,
    ): SnippetResponse = snippetService.getSnippetById(id, user.email)

    @GetMapping("/public/{id}")
    @Operation(summary = "Get public snippet", description = "Returns a public snippet by ID (no authentication required)")
    @ApiResponse(
        responseCode = "200",
        description = "Public snippet found",
        content = [Content(schema = Schema(implementation = SnippetResponse::class))],
    )
    @SecurityRequirements()
    @ApiResponse(responseCode = "404", description = "Snippet not found or not public")
    fun getByIdPublic(
        @PathVariable @Parameter(description = "Snippet UUID") id: UUID,
    ): SnippetResponse = snippetService.getPublic(id)

    @GetMapping("/search")
    @Operation(summary = "Search snippets", description = "Full-text search across snippet name, description, file contents, and tag names")
    @ApiResponse(
        responseCode = "200",
        description = "Paginated search results",
        content = [Content(schema = Schema(implementation = SnippetResponse::class))],
    )
    fun search(
        @AuthenticationPrincipal user: User,
        @RequestParam @Parameter(description = "Search query") q: String?,
        @RequestParam(defaultValue = "0") @Parameter page: Int,
        @RequestParam(defaultValue = "20") @Parameter size: Int
    ): Page<SnippetResponse> {
        val p = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        return snippetService.searchSnippets(q, user.email, p)
    }

    @PostMapping
    @Operation(summary = "Create snippet", description = "Creates a new snippet for the authenticated user")
    @ApiResponse(
        responseCode = "201",
        description = "Snippet created",
        content = [Content(schema = Schema(implementation = SnippetResponse::class))],
    )
    @ApiResponse(responseCode = "400", description = "Validation error or invalid tag IDs")
    fun create(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody dto: SnippetCreateRequest,
    ): ResponseEntity<SnippetResponse> {
        val snippet = snippetService.createSnippet(dto, user.email)
        return ResponseEntity.status(HttpStatus.CREATED).body(snippet)
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update snippet", description = "Partially updates an existing snippet")
    @ApiResponse(
        responseCode = "200",
        description = "Snippet updated",
        content = [Content(schema = Schema(implementation = SnippetResponse::class))],
    )
    @ApiResponse(responseCode = "404", description = "Snippet not found")
    fun update(
        @AuthenticationPrincipal user: User,
        @PathVariable @Parameter(description = "Snippet UUID") id: UUID,
        @Valid @RequestBody dto: SnippetCreateRequest,
    ): SnippetResponse = snippetService.updateSnippet(dto, user.email, id)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete snippet", description = "Deletes a snippet owned by the authenticated user")
    @ApiResponse(responseCode = "204", description = "Snippet deleted")
    @ApiResponse(responseCode = "404", description = "Snippet not found")
    fun delete(
        @AuthenticationPrincipal user: User,
        @PathVariable @Parameter(description = "Snippet UUID (can be with or without hyphens)") id: String,
    ): ResponseEntity<Unit> {
        snippetService.deleteSnippet(id, user.email)
        return ResponseEntity.noContent().build()
    }
}
