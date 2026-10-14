package com.example.data.model

data class MemoryCategoryItem(
    val name: String,
    val emoji: String,
    val description: String = ""
)

object MemoryCategories {
    val ALL_CATEGORIES = listOf(
        MemoryCategoryItem("First Meetup", "🤝", "The unforgettable day you met"),
        MemoryCategoryItem("First Love", "💖", "When feelings first sparked"),
        MemoryCategoryItem("First Crush", "🌸", "Butterflies and stolen glances"),
        MemoryCategoryItem("Last Breakup", "💔", "Closure and bittersweet memories"),
        MemoryCategoryItem("First Job", "💼", "Beginning of a professional journey"),
        MemoryCategoryItem("First Salary", "💰", "First hard-earned paycheck celebration"),
        MemoryCategoryItem("First Phone", "📱", "Connecting with the world"),
        MemoryCategoryItem("Graduation", "🎓", "Caps in the air, dreams ahead"),
        MemoryCategoryItem("Birthday", "🎂", "Another year around the sun"),
        MemoryCategoryItem("Anniversary", "💍", "Celebrating shared milestones"),
        MemoryCategoryItem("Family Moment", "🏡", "Warmth of home and loved ones"),
        MemoryCategoryItem("Lost Friendship", "🍃", "Cherishing bonds that once were"),
        MemoryCategoryItem("Last Voicemail", "📼", "A voice you'll keep forever"),
        MemoryCategoryItem("Achievement", "🏆", "Overcoming odds and winning"),
        MemoryCategoryItem("Travel & Adventure", "✈️", "New horizons, tickets and paths"),
        MemoryCategoryItem("Pet Companion", "🐾", "Unconditional love from furry friends"),
        MemoryCategoryItem("Childhood", "🧸", "Innocence, playground and nostalgia"),
        MemoryCategoryItem("Late Night Talk", "🌙", "Hours passing like minutes"),
        MemoryCategoryItem("Concert & Music", "🎵", "Songs that define a season"),
        MemoryCategoryItem("Secret Wish", "✨", "Wishes whispered to the stars"),
        MemoryCategoryItem("Everyday & Random", "☕", "Simple ordinary moments of joy"),
        MemoryCategoryItem("Other", "📝", "Any custom personal milestone")
    )

    val categoryNames: List<String> = ALL_CATEGORIES.map { it.name }

    fun getEmoji(categoryName: String): String {
        return ALL_CATEGORIES.find { it.name.equals(categoryName, ignoreCase = true) }?.emoji ?: "✨"
    }
}
