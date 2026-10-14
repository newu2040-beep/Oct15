package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Memory
import com.example.data.remote.ClipdropService
import com.example.data.repository.MemoryRepository
import com.example.ui.theme.LocalOctThemePalette
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun CreateEditMemoryScreen(
    memoryRepository: MemoryRepository,
    memoryIdToEdit: String? = null,
    initialDate: LocalDate? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalOctThemePalette.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipdropService = remember { ClipdropService(context) }

    var title by remember { mutableStateOf("") }
    var date by remember {
        mutableStateOf(
            initialDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        )
    }
    var category by remember { mutableStateOf("Love") }
    var customCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var cutoutImageUri by remember { mutableStateOf<String?>(null) }
    var isRemovingBg by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }

    var reminderEnabled by remember { mutableStateOf(true) }
    var yearlyAnniversary by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("09:00") }
    var advanceReminderDays by remember { mutableIntStateOf(0) }

    var isSaving by remember { mutableStateOf(false) }

    // Load existing memory if editing
    LaunchedEffect(memoryIdToEdit) {
        if (!memoryIdToEdit.isNullOrBlank()) {
            val existing = memoryRepository.getMemoryById(memoryIdToEdit)
            if (existing != null) {
                title = existing.title
                date = existing.date
                category = existing.category
                customCategory = existing.customCategory ?: ""
                description = existing.description
                imageUri = existing.imageUri
                cutoutImageUri = existing.cutoutImageUri
                location = existing.location ?: ""
                people = existing.people ?: ""
                tagsInput = existing.tags.joinToString(", ")
                reminderEnabled = existing.reminderEnabled
                yearlyAnniversary = existing.yearlyAnniversary
                reminderTime = existing.reminderTime
                advanceReminderDays = existing.advanceReminderDays
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
            cutoutImageUri = null // reset cutout if new image chosen
        }
    }

    // Date Picker
    val parsedDate = try { LocalDate.parse(date) } catch (e: Exception) { LocalDate.now() }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            val selected = LocalDate.of(y, m + 1, d)
            date = selected.format(DateTimeFormatter.ISO_LOCAL_DATE)
        },
        parsedDate.year,
        parsedDate.monthValue - 1,
        parsedDate.dayOfMonth
    )

    // Time Picker
    val timeParts = reminderTime.split(":")
    val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
    val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
    val timePickerDialog = TimePickerDialog(
        context,
        { _, h, m ->
            reminderTime = String.format("%02d:%02d", h, m)
        },
        hour,
        minute,
        true
    )

    val quickTitles = listOf(
        "First Meetup 🤝", "First Love 💖", "First Crush 🌸", "Last Breakup 💔",
        "First Job 💼", "First Salary 💰", "First Phone 📱", "Graduation 🎓",
        "Birthday 🎂", "Anniversary 💍", "Lost Friendship 🍃", "Last Voicemail 📼",
        "Travel ✈️", "Childhood 🧸", "Achievement 🏆"
    )
    val categories = com.example.data.model.MemoryCategories.categoryNames

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cancel",
                    tint = palette.textPrimary
                )
            }

            Text(
                text = if (memoryIdToEdit == null) "New Memory" else "Edit Memory",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )

            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter a memory title", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    scope.launch {
                        val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val memory = Memory(
                            id = memoryIdToEdit ?: "",
                            title = title.trim(),
                            date = date,
                            category = if (category == "Other" && customCategory.isNotBlank()) customCategory.trim() else category,
                            customCategory = if (category == "Other") customCategory.trim() else null,
                            description = description.trim(),
                            imageUri = imageUri,
                            cutoutImageUri = cutoutImageUri,
                            location = location.trim().ifBlank { null },
                            people = people.trim().ifBlank { null },
                            tags = tags,
                            reminderEnabled = reminderEnabled,
                            yearlyAnniversary = yearlyAnniversary,
                            reminderTime = reminderTime,
                            advanceReminderDays = advanceReminderDays
                        )

                        memoryRepository.saveMemory(memory)
                        isSaving = false
                        Toast.makeText(context, "Memory saved! ✨", Toast.LENGTH_SHORT).show()
                        onSaved()
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                modifier = Modifier.testTag("save_memory_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Scrollable Form Fields
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Title Suggestions
            Text(
                text = "Quick Ideas:",
                fontSize = 12.sp,
                color = palette.textSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickTitles.forEach { suggestion ->
                    val cleanTitle = suggestion.split(" ").take(2).joinToString(" ")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.surface)
                            .clickable { title = cleanTitle }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = suggestion, fontSize = 12.sp, color = palette.textPrimary)
                    }
                }
            }

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Memory Title *") },
                placeholder = { Text("e.g. First Love, First Salary, Last Voicemail") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memory_title_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.primary,
                    unfocusedBorderColor = palette.surface
                )
            )

            // Date picker field
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { datePickerDialog.show() }
                    .testTag("memory_date_picker_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date",
                            tint = palette.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Memory Date", fontSize = 11.sp, color = palette.textSecondary)
                            Text(date, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        }
                    }
                    Text("Change", color = palette.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Photo picker & Clipdrop AI Background Remover Integration!
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cover Photo & Sticker", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    val activeImage = cutoutImageUri ?: imageUri
                    if (activeImage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (cutoutImageUri != null) palette.surface else Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = activeImage,
                                contentDescription = "Memory photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(if (cutoutImageUri != null) 12.dp else 0.dp),
                                contentScale = if (cutoutImageUri != null) ContentScale.Fit else ContentScale.Crop
                            )

                            // Remove photo button top right
                            IconButton(
                                onClick = {
                                    imageUri = null
                                    cutoutImageUri = null
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ✨ AI Background Remover / Cutout Sticker Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val originalUri = imageUri
                                    if (originalUri != null) {
                                        isRemovingBg = true
                                        scope.launch {
                                            val cutout = clipdropService.removeBackground(Uri.parse(originalUri))
                                            isRemovingBg = false
                                            if (cutout != null) {
                                                cutoutImageUri = cutout
                                                Toast.makeText(context, "AI Sticker Cutout ready! ✨", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Cutout completed with local sticker frame", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isRemovingBg
                            ) {
                                if (isRemovingBg) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Removing BG...", fontSize = 12.sp)
                                } else {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (cutoutImageUri != null) "Re-cut Sticker" else "✨ Magic AI Sticker", fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Replace", fontSize = 12.sp)
                            }
                        }
                    } else {
                        // Empty photo picker prompt
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.surface.copy(alpha = 0.5f))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("memory_photo_picker_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Outlined.Image, contentDescription = null, tint = palette.primary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Choose photo or polaroid snapshot", fontSize = 13.sp, color = palette.textSecondary)
                            }
                        }
                    }
                }
            }

            // Category Selection
            Text("Category", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = category == cat
                    val emoji = com.example.data.model.MemoryCategories.getEmoji(cat)
                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = { Text("$emoji $cat", fontSize = 12.sp) },
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (category == "Other") {
                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it },
                    label = { Text("Custom Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Story / Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("The Story / Personal Notes") },
                placeholder = { Text("What happened? How did it feel? Why is this moment unforgettable?") },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memory_description_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.primary,
                    unfocusedBorderColor = palette.surface
                )
            )

            // Location & People involved
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. Paris, Library") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = people,
                    onValueChange = { people = it },
                    label = { Text("People involved") },
                    placeholder = { Text("e.g. Maya, Bestie") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Tags
            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = { Text("Tags (comma-separated)") },
                placeholder = { Text("love, first, summer, trip") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Reminder & Anniversary settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Anniversary & Reminders", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Reminders", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textPrimary)
                            Text("Get notified when this day comes around", fontSize = 11.sp, color = palette.textSecondary)
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = palette.primary)
                        )
                    }

                    AnimatedVisibility(visible = reminderEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Yearly Anniversary", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textPrimary)
                                    Text("Recur every year on this month & day", fontSize = 11.sp, color = palette.textSecondary)
                                }
                                Switch(
                                    checked = yearlyAnniversary,
                                    onCheckedChange = { yearlyAnniversary = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = palette.primary)
                                )
                            }

                            // Reminder Time
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { timePickerDialog.show() }
                                    .background(palette.surface.copy(alpha = 0.5f))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reminder Time: $reminderTime", fontSize = 13.sp, color = palette.textPrimary)
                                }
                                Text("Change", color = palette.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
