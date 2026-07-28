package com.codesave.backend.controller

import com.codesave.backend.dto.snippet.SnippetResponse
import com.codesave.backend.dto.tag.TagCreateRequest
import com.codesave.backend.dto.tag.TagResponse
import com.codesave.backend.entity.User
import com.codesave.backend.service.TagService
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/tag")
@Tag(name = "Tag", description = "Tag management operations")
class TagController(
    private val tagService: TagService
) {

    @PostMapping
    @Operation(summary = "Create tag", description = "Creates a new tag for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Tag created", content = [Content(schema = Schema(implementation = TagResponse::class))])
    fun createTag(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody dto: TagCreateRequest
    ): TagResponse {
        return tagService.createTag(dto, user.email)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tag", description = "Deletes a tag owned by the authenticated user")
    @ApiResponse(responseCode = "204", description = "Tag deleted")
    @ApiResponse(responseCode = "404", description = "Tag not found")
    fun deleteTag(
        @AuthenticationPrincipal user: User,
        @PathVariable @Parameter(description = "Tag UUID") id: UUID
    ): ResponseEntity<Unit> {
        tagService.deleteTag(id, user.email)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/all")
    @Operation(summary = "Get all tags", description = "Returns a paginated list of the authenticated user's tags")
    @ApiResponse(responseCode = "200", description = "Paginated list of tags", content = [Content(schema = Schema(implementation = TagResponse::class))])
    fun getAllTags(
        @AuthenticationPrincipal user: User,
        @PageableDefault(size = 20)
        @Parameter(description = "Pagination parameters (page, size)")
        pageable: Pageable
    ): Page<TagResponse> {
        return tagService.getAllTags(user.email, pageable)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID", description = "Returns a single tag owned by the authenticated user")
    @ApiResponse(responseCode = "200", description = "Tag found", content = [Content(schema = Schema(implementation = TagResponse::class))])
    @ApiResponse(responseCode = "404", description = "Tag not found")
    fun getTagById(
        @AuthenticationPrincipal user: User,
        @PathVariable @Parameter(description = "Tag UUID") id: UUID
    ): TagResponse {
        return tagService.getTagById(id, user.email)
    }

    @GetMapping("/{id}/snippets")
    @Operation(summary = "Get snippets by tag", description = "Returns all snippets associated with a given tag")
    @ApiResponse(responseCode = "200", description = "List of snippets", content = [Content(schema = Schema(implementation = SnippetResponse::class))])
    @ApiResponse(responseCode = "404", description = "Tag not found")
    fun getSnippetsByTagId(
        @AuthenticationPrincipal user: User,
        @PathVariable @Parameter(description = "Tag UUID") id: UUID
    ): List<SnippetResponse> {
        return tagService.getAllSnippetsByTagId(id, user.email)
    }
}