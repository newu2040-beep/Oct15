package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Memory
import com.example.data.repository.MemoryRepository
import com.example.ui.components.MemoryCard
import com.example.ui.theme.LocalOctThemePalette
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    memoryRepository: MemoryRepository,
    onMemoryClick: (String) -> Unit,
    onAddMemoryForDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalOctThemePalette.current
    val memories by memoryRepository.memoriesFlow.collectAsState(initial = emptyList())

    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfMonth = currentYearMonth.atDay(1).dayOfWeek.value % 7 // 0 = Sun, 1 = Mon ...

    val totalCells = firstDayOfMonth + daysInMonth

    // Memories on the selected date (or anniversary on this month and day)
    val selectedDateMemories = memories.filter {
        it.parseLocalDate() == selectedDate || it.isAnniversaryOn(selectedDate)
    }

    com.example.ui.components.ScrapbookBackground(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Month and Year Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Text(
                    text = "Visual Memory Calendar",
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Jump to Today button
                TextButton(
                    onClick = {
                        val today = LocalDate.now()
                        currentYearMonth = YearMonth.of(today.year, today.month)
                        selectedDate = today
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Today,
                        contentDescription = "Today",
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Today", color = palette.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = palette.textPrimary
                    )
                }

                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        tint = palette.textPrimary
                    )
                }
            }
        }

        // Days of week header (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Monthly Grid with cute stickers on dates with memories
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
            border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                userScrollEnabled = false
            ) {
                // Empty spacer items before day 1
                items(firstDayOfMonth) {
                    Box(modifier = Modifier.aspectRatio(1f))
                }

                // Month days
                items(daysInMonth) { index ->
                    val dayNum = index + 1
                    val date = currentYearMonth.atDay(dayNum)
                    val isToday = date == LocalDate.now()
                    val isSelected = date == selectedDate

                    // Find memories on this day
                    val dayMemories = memories.filter {
                        it.parseLocalDate() == date || it.isAnniversaryOn(date)
                    }
                    val firstMemoryWithPhoto = dayMemories.firstOrNull { !it.cutoutImageUri.isNullOrBlank() || !it.imageUri.isNullOrBlank() }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> palette.primary
                                    isToday -> palette.surface
                                    dayMemories.isNotEmpty() -> palette.surface.copy(alpha = 0.5f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { selectedDate = date }
                            .testTag("calendar_day_$dayNum"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Date Number
                            Text(
                                text = dayNum.toString(),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> Color.White
                                    isToday -> palette.primary
                                    else -> palette.textPrimary
                                }
                            )

                            // Sticker or dot indicator
                            if (dayMemories.isNotEmpty()) {
                                if (firstMemoryWithPhoto != null && !isSelected) {
                                    // Mini photo sticker
                                    val photo = firstMemoryWithPhoto.cutoutImageUri ?: firstMemoryWithPhoto.imageUri
                                    AsyncImage(
                                        model = photo,
                                        contentDescription = "Memory sticker",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color.White, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Colored memory dot
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color.White
                                                else Color(0xFFFF5E8A)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Details Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")),
                color = palette.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { onAddMemoryForDate(selectedDate) }) {
                Text("+ Add Memory", color = palette.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (selectedDateMemories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No memories recorded for this date.\nTap '+ Add Memory' to save a moment.",
                    color = palette.textSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(selectedDateMemories, key = { it.id }) { memory ->
                    MemoryCard(
                        memory = memory,
                        onClick = { onMemoryClick(memory.id) },
                        onReminderToggle = {}
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }
}
}


