package com.example.data.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "Rahul Shah",
    val email: String = "rahul.shah@gmail.com",
    val photoUrl: String? = null,
    val bio: String = "Collecting moments, not things.",
    val gender: String = "Male",
    val age: String = "28 years",
    val location: String = "Kathmandu, Nepal",
    val joinedDate: String = "Oct 15, 2025",
    val onboardingCompleted: Boolean = false,
    val themeName: String = "Lavender Dream",
    val backgroundStyle: String = "Pastel",
    val fontStyle: String = "Default",
    val notificationsEnabled: Boolean = true,
    val callReminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
