package com.codesave.backend.user

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserDetailsServiceImpl(
    private final val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(id: String): UserDetails {
        val user = userRepository.findById(UUID.fromString(id))
        if (user.isEmpty) {
            throw UsernameNotFoundException("User $id not found")
        }
        return user.get()
    }
}
