package com.codesave.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import java.time.Instant
import java.util.UUID


@Entity
@Table(name = "snippet")
class Snippet(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, length = 256)
    var name: String,

    @Column(nullable = false, columnDefinition = "text")
    var description: String,

    @Column(nullable = false, length = 256)
    var language: String,

    @Column(nullable = false, columnDefinition = "text")
    var code: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(nullable = false, name = "is_public")
    var isPublic: Boolean,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tag_snippet",
        joinColumns = [JoinColumn(name = "snippet_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    @BatchSize(size = 50)
    var tags: MutableList<Tag> = mutableListOf()
) {
    @PrePersist
    fun onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now()
        }
    }
}