package com.codesave.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "snippet_file",
    uniqueConstraints = [
        UniqueConstraint(name = "idx_snippet_file_unique_filename", columnNames = ["snippet_id", "filename"]),
    ],
)
class SnippetFile(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "snippet_id", nullable = false)
    var snippet: Snippet,

    @Column(nullable = false, length = 256)
    var filename: String,

    @Column(nullable = false, columnDefinition = "text")
    var code: String,
)
