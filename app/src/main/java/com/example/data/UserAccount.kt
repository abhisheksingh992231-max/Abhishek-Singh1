package com.example.data

enum class AuthProvider(val displayName: String) {
    GOOGLE("Google"),
    FACEBOOK("Facebook"),
    GUEST("Guest")
}

data class UserAccount(
    val name: String,
    val email: String,
    val provider: AuthProvider,
    val avatarInitials: String,
    val isLoggedIn: Boolean = true
) {
    companion object {
        fun defaultGuest() = UserAccount(
            name = "Guest User",
            email = "guest@pdftoolkit.local",
            provider = AuthProvider.GUEST,
            avatarInitials = "GS",
            isLoggedIn = true
        )

        fun google(name: String = "Google User", email: String = "user@gmail.com") = UserAccount(
            name = name,
            email = email,
            provider = AuthProvider.GOOGLE,
            avatarInitials = getInitials(name, "GU"),
            isLoggedIn = true
        )

        fun facebook(name: String = "Facebook User", email: String = "user@facebook.com") = UserAccount(
            name = name,
            email = email,
            provider = AuthProvider.FACEBOOK,
            avatarInitials = getInitials(name, "FB"),
            isLoggedIn = true
        )

        private fun getInitials(name: String, fallback: String): String {
            val parts = name.trim().split(" ").filter { it.isNotEmpty() }
            return when {
                parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
                parts.size == 1 && parts[0].length >= 2 -> parts[0].substring(0, 2).uppercase()
                parts.size == 1 -> "${parts[0].first()}".uppercase()
                else -> fallback
            }
        }
    }
}
