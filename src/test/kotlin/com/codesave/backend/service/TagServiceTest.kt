package com.codesave.backend.service

import com.codesave.backend.dto.tag.TagCreateRequest
import com.codesave.backend.entity.Snippet
import com.codesave.backend.entity.Tag
import com.codesave.backend.entity.User
import com.codesave.backend.exception.TagNotFoundException
import com.codesave.backend.repotisotry.TagRepository
import com.codesave.backend.repotisotry.UserRepository
import com.codesave.backend.service.impl.TagServiceImpl
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
class TagServiceTest {

    @Mock
    private lateinit var tagRepository: TagRepository

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var tagService: TagServiceImpl

    @BeforeEach
    fun setUp() {
        tagService = TagServiceImpl(tagRepository, userRepository)
    }

    // ---------- createTag ----------

    @Test
    fun `createTag persists tag when user exists`() {
        val dto = TagCreateRequest(name = "important")
        val user = User(email = "user@example.com")

        whenever(userRepository.findByEmail("user@example.com")).thenReturn(user)
        whenever(tagRepository.save(any<Tag>())).thenAnswer { inv ->
            val t = inv.getArgument<Tag>(0)
            t.id = UUID.randomUUID()
            t
        }

        val result = tagService.createTag(dto, "user@example.com")

        val captor = argumentCaptor<Tag>()
        verify(tagRepository).save(captor.capture())
        val persisted = captor.firstValue
        assert(persisted.name == "important")
        assert(persisted.user == user)
        assert(result.name == "important")
    }

    @Test
    fun `createTag throws when user not found`() {
        val dto = TagCreateRequest(name = "important")

        whenever(userRepository.findByEmail("missing@example.com")).thenReturn(null)

        assertThrows<NullPointerException> {
            tagService.createTag(dto, "missing@example.com")
        }
        verify(tagRepository, never()).save(any())
    }

    // ---------- deleteTag ----------

    @Test
    fun `deleteTag deletes when tag exists and owned by user`() {
        val tagId = UUID.randomUUID()
        val user = User(email = "owner@example.com")
        val tag = Tag(name = "tag", user = user).also { it.id = tagId }

        whenever(tagRepository.findById(tagId)).thenReturn(Optional.of(tag))

        tagService.deleteTag(tagId, "owner@example.com")

        verify(tagRepository).delete(tag)
    }

    @Test
    fun `deleteTag throws when tag does not exist`() {
        val tagId = UUID.randomUUID()

        whenever(tagRepository.findById(tagId)).thenReturn(Optional.empty())

        assertThrows<TagNotFoundException> {
            tagService.deleteTag(tagId, "owner@example.com")
        }
        verify(tagRepository, never()).delete(any())
    }

    @Test
    fun `deleteTag throws when tag owned by different user`() {
        val tagId = UUID.randomUUID()
        val otherUser = User(email = "other@example.com")
        val tag = Tag(name = "tag", user = otherUser).also { it.id = tagId }

        whenever(tagRepository.findById(tagId)).thenReturn(Optional.of(tag))

        assertThrows<TagNotFoundException> {
            tagService.deleteTag(tagId, "requester@example.com")
        }
        verify(tagRepository, never()).delete(any())
    }

    // ---------- getAllTags ----------

    @Test
    fun `getAllTags delegates to repository with same args`() {
        val pageable = Pageable.ofSize(10)
        val page: Page<Tag> = PageImpl(emptyList())
        whenever(tagRepository.findAllByUserEmail("user@example.com", pageable)).thenReturn(page)

        val result = tagService.getAllTags("user@example.com", pageable)

        verify(tagRepository).findAllByUserEmail("user@example.com", pageable)
        assert(result.totalElements == 0L)
    }

    // ---------- getTagById ----------

    @Test
    fun `getTagById returns tag when found`() {
        val tagId = UUID.randomUUID()
        val tag = Tag(name = "found", user = User(email = "user@example.com"))
            .also { it.id = tagId }

        whenever(tagRepository.findByIdAndUserEmail(tagId, "user@example.com"))
            .thenReturn(Optional.of(tag))

        val result = tagService.getTagById(tagId, "user@example.com")

        assert(result.name == "found")
    }

    @Test
    fun `getTagById throws when not found`() {
        val tagId = UUID.randomUUID()

        whenever(tagRepository.findByIdAndUserEmail(tagId, "user@example.com"))
            .thenReturn(Optional.empty())

        assertThrows<TagNotFoundException> {
            tagService.getTagById(tagId, "user@example.com")
        }
    }

    // ---------- getAllSnippetsByTagId ----------

    @Test
    fun `getAllSnippetsByTagId returns snippets when tag found`() {
        val tagId = UUID.randomUUID()
        val user = User(email = "user@example.com")
        val snippets = listOf(
            Snippet(name = "s1", description = "", language = "", code = "", isPublic = false, user = user)
                .also { it.id = UUID.randomUUID(); it.createdAt = Instant.now() },
            Snippet(name = "s2", description = "", language = "", code = "", isPublic = false, user = user)
                .also { it.id = UUID.randomUUID(); it.createdAt = Instant.now() }
        )
        val tag = Tag(name = "tag", user = user).also {
            it.id = tagId
            it.snippets = snippets.toMutableList()
        }

        whenever(tagRepository.findByIdAndUserEmail(tagId, "user@example.com"))
            .thenReturn(Optional.of(tag))

        val result = tagService.getAllSnippetsByTagId(tagId, "user@example.com")

        assert(result.size == 2)
        assert(result[0].name == "s1")
        assert(result[1].name == "s2")
    }

    @Test
    fun `getAllSnippetsByTagId throws when tag not found`() {
        val tagId = UUID.randomUUID()

        whenever(tagRepository.findByIdAndUserEmail(tagId, "user@example.com"))
            .thenReturn(Optional.empty())

        assertThrows<TagNotFoundException> {
            tagService.getAllSnippetsByTagId(tagId, "user@example.com")
        }
    }
}
