package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Memory
import com.example.ui.theme.LocalOctThemePalette
import java.time.LocalDate

@Composable
fun MemoryCard(
    memory: Memory,
    onClick: () -> Unit,
    onReminderToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalOctThemePalette.current

    // Category emoji & pastel background tint mapping
    val (emoji, bgTint) = when (memory.category.lowercase()) {
        "love" -> Pair("❤️", Color(0xFFFFF0F3))
        "career" -> Pair("💼", Color(0xFFEFF6FF))
        "milestones" -> Pair("📱", Color(0xFFF0FDF4))
        "school" -> Pair("🎓", Color(0xFFFEF9C3))
        "friendship" -> Pair("✨", Color(0xFFF5F3FF))
        "family" -> Pair("🏡", Color(0xFFFFF7ED))
        "travel" -> Pair("✈️", Color(0xFFECFEFF))
        "personal growth" -> Pair("💔", Color(0xFFFFF1F2))
        else -> Pair("🌱", Color(0xFFFAF5FF))
    }

    val cardBg = if (palette.isDark) palette.cardBackground else bgTint

    val isTodayAnniversary = memory.isAnniversaryOn(LocalDate.now())
    val years = memory.yearsSince(LocalDate.now())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("memory_card_${memory.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (palette.isDark) Color(0xFF373052) else Color(0xFFF3E8FF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left content: Date, Title with emoji, and anniversary indicator
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = memory.formattedDate(),
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = memory.title,
                        color = palette.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = emoji,
                        fontSize = 16.sp
                    )
                }

                if (isTodayAnniversary && years > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFF7597))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Today! • $years years anniversary 🎉",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right content: Polaroid photo thumbnail + Bell reminder toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Polaroid photo miniature
                Box(
                    modifier = Modifier
                        .size(54.dp, 58.dp)
                        .rotate(2f)
                        .shadow(3.dp, RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val displayImage = memory.cutoutImageUri ?: memory.imageUri
                    if (!displayImage.isNullOrBlank()) {
                        AsyncImage(
                            model = displayImage,
                            contentDescription = memory.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(palette.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                // Bell reminder button
                IconButton(
                    onClick = onReminderToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (memory.reminderEnabled) palette.primary.copy(alpha = 0.12f)
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        imageVector = if (memory.reminderEnabled) Icons.Filled.Notifications
                        else Icons.Outlined.NotificationsNone,
                        contentDescription = "Toggle reminder",
                        tint = if (memory.reminderEnabled) palette.primary
                        else palette.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
