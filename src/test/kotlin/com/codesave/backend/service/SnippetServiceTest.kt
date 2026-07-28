package com.codesave.backend.service

import com.codesave.backend.dto.snippet.SnippetCreateRequest
import com.codesave.backend.entity.Snippet
import com.codesave.backend.entity.Tag
import com.codesave.backend.entity.User
import com.codesave.backend.exception.SnippetNotFoundException
import com.codesave.backend.repotisotry.SnippetRepository
import com.codesave.backend.repotisotry.TagRepository
import com.codesave.backend.repotisotry.UserRepository
import com.codesave.backend.service.impl.SnippetServiceImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SnippetServiceTest {

    @Mock
    private lateinit var snippetRepository: SnippetRepository

    @Mock
    private lateinit var tagRepository: TagRepository

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var snippetService: SnippetServiceImpl

    @BeforeEach
    fun setUp() {
        snippetService = SnippetServiceImpl(snippetRepository, tagRepository, userRepository)
    }

    // ---------- getAllSnippets ----------

    @Test
    fun `getAllSnippets delegates to repository`() {
        val user = User(email = "user@example.com")
        val snippet = Snippet(
            name = "test", description = "desc", language = "kotlin",
            code = "code", isPublic = false, user = user
        ).also { it.id = UUID.randomUUID(); it.createdAt = Instant.now() }
        val pageable = Pageable.ofSize(10)
        val page: Page<Snippet> = PageImpl(listOf(snippet))
        whenever(snippetRepository.findAllByUserEmail("user@example.com", pageable)).thenReturn(page)

        val result = snippetService.getAllSnippets("user@example.com", pageable)

        verify(snippetRepository).findAllByUserEmail("user@example.com", pageable)
        assert(result.content.size == 1)
        assert(result.content.first().name == "test")
    }

    // ---------- createSnippet ----------

    @Test
    fun `createSnippet persists snippet without tags`() {
        val dto = SnippetCreateRequest(
            name = "my snippet",
            description = "desc",
            language = "java",
            code = "System.out.println(1);",
            isPublic = false,
            tagIds = null
        )
        val user = User(email = "user@example.com")

        whenever(userRepository.findByEmail("user@example.com")).thenReturn(user)
        whenever(snippetRepository.save(any<Snippet>())).thenAnswer { inv ->
            val s = inv.getArgument<Snippet>(0)
            s.id = UUID.randomUUID()
            s.createdAt = Instant.now()
            s
        }

        val result = snippetService.createSnippet(dto, "user@example.com")

        val captor = argumentCaptor<Snippet>()
        verify(snippetRepository).save(captor.capture())
        val persisted = captor.firstValue
        assert(persisted.name == "my snippet")
        assert(persisted.description == "desc")
        assert(persisted.language == "java")
        assert(persisted.isPublic == false)
        assert(persisted.code == "System.out.println(1);")
        assert(persisted.user == user)
        assert(persisted.createdAt != null)
        assert(result.name == "my snippet")
        verify(tagRepository, never()).findByIdInAndUserEmail(any(), any())
    }

    @Test
    fun `createSnippet attaches tags when tagIds provided`() {
        val tagId1 = UUID.randomUUID()
        val tagId2 = UUID.randomUUID()
        val dto = SnippetCreateRequest(
            name = "tagged snippet", description = "desc", language = "java",
            code = "code", isPublic = false,
            tagIds = listOf(tagId1, tagId2)
        )
        val user = User(email = "user@example.com")
        val tags = listOf(Tag(name = "a", user = User()), Tag(name = "b", user = User()))

        whenever(userRepository.findByEmail("user@example.com")).thenReturn(user)
        whenever(tagRepository.findByIdInAndUserEmail(listOf(tagId1, tagId2), "user@example.com"))
            .thenReturn(tags)
        whenever(snippetRepository.save(any<Snippet>())).thenAnswer { inv ->
            val s = inv.getArgument<Snippet>(0)
            s.id = UUID.randomUUID(); s.createdAt = Instant.now(); s
        }

        snippetService.createSnippet(dto, "user@example.com")

        val captor = argumentCaptor<Snippet>()
        verify(snippetRepository).save(captor.capture())
        assert(captor.firstValue.tags == tags)
    }

    @Test
    fun `createSnippet throws when user not found`() {
        val dto = SnippetCreateRequest(name = "snippet")

        whenever(userRepository.findByEmail("missing@example.com")).thenReturn(null)

        assertThrows<NullPointerException> {
            snippetService.createSnippet(dto, "missing@example.com")
        }
        verify(snippetRepository, never()).save(any())
    }

    // ---------- updateSnippet ----------

    @Test
    fun `updateSnippet updates only provided fields`() {
        val snippetId = UUID.randomUUID()
        val existing = Snippet(
            name = "old name", description = "old description",
            code = "old code", language = "python", isPublic = false,
            user = User(email = "user@example.com")
        ).also { it.id = snippetId; it.createdAt = Instant.now() }

        val dto = SnippetCreateRequest(name = "new name")

        whenever(snippetRepository.findByIdAndUserEmail(snippetId, "user@example.com"))
            .thenReturn(Optional.of(existing))

        snippetService.updateSnippet(dto, "user@example.com", snippetId)

        assert(existing.name == "new name")
        assert(existing.description == "old description")
        assert(existing.code == "old code")
        assert(existing.language == "python")
        assert(existing.isPublic == false)
        verify(tagRepository, never()).findByIdInAndUserEmail(any(), any())
    }

    @Test
    fun `updateSnippet updates tags when tagIds provided and valid`() {
        val snippetId = UUID.randomUUID()
        val tagId = UUID.randomUUID()
        val existing = Snippet(
            name = "", description = "", language = "", code = "", isPublic = false,
            user = User(email = "user@example.com")
        ).also { it.id = snippetId; it.createdAt = Instant.now() }
        val dto = SnippetCreateRequest(tagIds = listOf(tagId))
        val tags = listOf(Tag(name = "a", user = User()))

        whenever(snippetRepository.findByIdAndUserEmail(snippetId, "user@example.com"))
            .thenReturn(Optional.of(existing))
        whenever(tagRepository.findByIdInAndUserEmail(listOf(tagId), "user@example.com"))
            .thenReturn(tags)

        snippetService.updateSnippet(dto, "user@example.com", snippetId)

        assert(existing.tags == tags)
    }

    @Test
    fun `updateSnippet throws when tagId count mismatch`() {
        val snippetId = UUID.randomUUID()
        val tagId1 = UUID.randomUUID()
        val tagId2 = UUID.randomUUID()
        val existing = Snippet(
            name = "", description = "", language = "", code = "", isPublic = false,
            user = User(email = "user@example.com")
        ).also { it.id = snippetId; it.createdAt = Instant.now() }
        val dto = SnippetCreateRequest(tagIds = listOf(tagId1, tagId2))
        val onlyOneTag = listOf(Tag(name = "a", user = User()))

        whenever(snippetRepository.findByIdAndUserEmail(snippetId, "user@example.com"))
            .thenReturn(Optional.of(existing))
        whenever(tagRepository.findByIdInAndUserEmail(listOf(tagId1, tagId2), "user@example.com"))
            .thenReturn(onlyOneTag)

        assertThrows<IllegalArgumentException> {
            snippetService.updateSnippet(dto, "user@example.com", snippetId)
        }
    }

    @Test
    fun `updateSnippet throws when snippet not found`() {
        val snippetId = UUID.randomUUID()
        val dto = SnippetCreateRequest()

        whenever(snippetRepository.findByIdAndUserEmail(snippetId, "user@example.com"))
            .thenReturn(Optional.empty())

        assertThrows<SnippetNotFoundException> {
            snippetService.updateSnippet(dto, "user@example.com", snippetId)
        }
    }

    // ---------- deleteSnippet ----------

    @Test
    fun `deleteSnippet deletes when found`() {
        val snippetId = UUID.randomUUID()
        val snippet = Snippet(
            name = "", description = "", language = "", code = "", isPublic = false,
            user = User(email = "user@example.com")
        ).also { it.id = snippetId }

        whenever(snippetRepository.findByIdAndUserEmail(snippetId, "user@example.com"))
            .thenReturn(Optional.of(snippet))

        snippetService.deleteSnippet(snippetId.toString(), "user@example.com")

        verify(snippetRepository).delete(snippet)
    }

    @Test
    fun `deleteSnippet throws when not found`() {
        val snippetId = UUID.randomUUID()

        whenever(snippetRepository.findByIdAndUserEmail(snippetId, "user@example.com"))
            .thenReturn(Optional.empty())

        assertThrows<SnippetNotFoundException> {
            snippetService.deleteSnippet(snippetId.toString(), "user@example.com")
        }
        verify(snippetRepository, never()).delete(any())
    }

    // ---------- getSnippetById ----------

    @Test
    fun `getSnippetById returns snippet when found`() {
        val snippetId = UUID.randomUUID()
        val snippet = Snippet(
            name = "found", description = "d", language = "l",
            code = "c", isPublic = false, user = User(email = "user@example.com")
        ).also { it.id = snippetId; it.createdAt = Instant.now() }

        whenever(snippetRepository.findByIdAndUserEmail(snippetId, "user@example.com"))
            .thenReturn(Optional.of(snippet))

        val result = snippetService.getSnippetById(snippetId.toString(), "user@example.com")

        assert(result.name == "found")
    }

    @Test
    fun `getSnippetById throws when not found`() {
        val snippetId = UUID.randomUUID()

        whenever(snippetRepository.findByIdAndUserEmail(snippetId, "user@example.com"))
            .thenReturn(Optional.empty())

        assertThrows<SnippetNotFoundException> {
            snippetService.getSnippetById(snippetId.toString(), "user@example.com")
        }
    }

    // ---------- searchSnippets ----------

    @Test
    fun `searchSnippets returns all when query blank`() {
        val pageable = Pageable.ofSize(10)
        val page: Page<Snippet> = PageImpl(emptyList())
        whenever(snippetRepository.findAllByUserEmail("user@example.com", pageable)).thenReturn(page)

        snippetService.searchSnippets("   ", "user@example.com", pageable)

        verify(snippetRepository).findAllByUserEmail("user@example.com", pageable)
    }

    @Test
    fun `searchSnippets returns all when query null`() {
        val pageable = Pageable.ofSize(10)
        val page: Page<Snippet> = PageImpl(emptyList())
        whenever(snippetRepository.findAllByUserEmail("user@example.com", pageable)).thenReturn(page)

        snippetService.searchSnippets(null, "user@example.com", pageable)

        verify(snippetRepository).findAllByUserEmail("user@example.com", pageable)
    }

    @Test
    fun `searchSnippets delegates to globalSearch when query provided`() {
        val user = User(email = "user@example.com").also { it.id = UUID.randomUUID() }
        val snippet = Snippet(
            name = "found", description = "", language = "",
            code = "", isPublic = false, user = user
        ).also { it.id = UUID.randomUUID(); it.createdAt = Instant.now() }
        val pageable = Pageable.ofSize(10)
        val page: Page<Snippet> = PageImpl(listOf(snippet))

        whenever(userRepository.findByEmail("user@example.com")).thenReturn(user)
        whenever(snippetRepository.globalSearch(user.id!!, "%hello%", pageable)).thenReturn(page)

        val result = snippetService.searchSnippets("hello", "user@example.com", pageable)

        assert(result.content.size == 1)
    }

    // ---------- getPublic ----------

    @Test
    fun `getPublic returns snippet when public`() {
        val snippetId = UUID.randomUUID()
        val snippet = Snippet(
            name = "public snippet", description = "", language = "",
            code = "", isPublic = true, user = User(email = "user@example.com")
        ).also { it.id = snippetId; it.createdAt = Instant.now() }

        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))

        val result = snippetService.getPublic(snippetId)

        assert(result.name == "public snippet")
    }

    @Test
    fun `getPublic throws when snippet not public`() {
        val snippetId = UUID.randomUUID()
        val snippet = Snippet(
            name = "private", description = "", language = "",
            code = "", isPublic = false, user = User(email = "user@example.com")
        ).also { it.id = snippetId; it.createdAt = Instant.now() }

        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))

        assertThrows<SnippetNotFoundException> {
            snippetService.getPublic(snippetId)
        }
    }

    @Test
    fun `getPublic throws when snippet does not exist`() {
        val snippetId = UUID.randomUUID()

        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.empty())

        assertThrows<SnippetNotFoundException> {
            snippetService.getPublic(snippetId)
        }
    }
}
