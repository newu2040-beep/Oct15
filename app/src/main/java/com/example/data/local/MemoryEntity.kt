package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Memory

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val date: String,
    val category: String,
    val customCategory: String?,
    val description: String,
    val imageUri: String?,
    val cutoutImageUri: String?,
    val location: String?,
    val people: String?,
    val tagsRaw: String, // comma separated tags
    val isFavorite: Boolean,
    val reminderEnabled: Boolean,
    val yearlyAnniversary: Boolean,
    val reminderTime: String,
    val advanceReminderDays: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean
) {
    fun toMemory(): Memory {
        return Memory(
            id = id,
            userId = userId,
            title = title,
            date = date,
            category = category,
            customCategory = customCategory,
            description = description,
            imageUri = imageUri,
            cutoutImageUri = cutoutImageUri,
            location = location,
            people = people,
            tags = if (tagsRaw.isBlank()) emptyList() else tagsRaw.split(",").map { it.trim() },
            isFavorite = isFavorite,
            reminderEnabled = reminderEnabled,
            yearlyAnniversary = yearlyAnniversary,
            reminderTime = reminderTime,
            advanceReminderDays = advanceReminderDays,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }

    companion object {
        fun fromMemory(memory: Memory): MemoryEntity {
            return MemoryEntity(
                id = memory.id,
                userId = memory.userId,
                title = memory.title,
                date = memory.date,
                category = memory.category,
                customCategory = memory.customCategory,
                description = memory.description,
                imageUri = memory.imageUri,
                cutoutImageUri = memory.cutoutImageUri,
                location = memory.location,
                people = memory.people,
                tagsRaw = memory.tags.joinToString(","),
                isFavorite = memory.isFavorite,
                reminderEnabled = memory.reminderEnabled,
                yearlyAnniversary = memory.yearlyAnniversary,
                reminderTime = memory.reminderTime,
                advanceReminderDays = memory.advanceReminderDays,
                createdAt = memory.createdAt,
                updatedAt = memory.updatedAt,
                isSynced = memory.isSynced
            )
        }
    }
}
