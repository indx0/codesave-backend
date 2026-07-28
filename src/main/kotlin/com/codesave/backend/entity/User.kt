package com.codesave.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = false, length = 256)
    var name: String = "",

    @Column(nullable = false, unique = true, length = 256)
    var email: String = "",

    @Column(nullable = false, name = "password", length = 256)
    var passwordHash: String? = ""
) : UserDetails {
    override fun getPassword(): String? = passwordHash
    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("USER"))
    override fun getUsername(): String = email
}

