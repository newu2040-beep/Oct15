package com.example.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.PreferencesDataStore
import com.example.data.model.Memory
import com.example.data.repository.MemoryRepository
import com.example.ui.theme.LocalOctThemePalette
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun OnboardingScreen(
    memoryRepository: MemoryRepository,
    preferencesDataStore: PreferencesDataStore,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalOctThemePalette.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(1) } // 1: First memory, 2: About yourself

    // Step 1: Starter memory
    var memoryTitle by remember { mutableStateOf("First Love") }
    var memoryDate by remember { mutableStateOf(LocalDate.now().minusYears(5).format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var memoryImageUri by remember { mutableStateOf<String?>(null) }
    var memoryNote by remember { mutableStateOf("") }

    // Step 2: User profile
    var userName by remember { mutableStateOf("Rahul Shah") }
    var userBio by remember { mutableStateOf("Collecting moments, not things.") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            memoryImageUri = uri.toString()
        }
    }

    // Date Picker Dialog helper
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val picked = LocalDate.of(year, month + 1, dayOfMonth)
            memoryDate = picked.format(DateTimeFormatter.ISO_LOCAL_DATE)
        },
        calendar.get(Calendar.YEAR) - 5,
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Top Row: Back button & Progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Progress Indicator matching the screenshot
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.primary)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (step >= 2) palette.primary else palette.surface)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (step == 1) {
                // Header matching showcase: "What was your first love? / Tell us a little about it. It helps us save the moment."
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "What was your\nfirst love?",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            lineHeight = 34.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tell us a little about it.\nIt helps us save the moment.",
                            fontSize = 14.sp,
                            color = palette.textSecondary,
                            lineHeight = 20.sp
                        )
                    }

                    // Little decorative stars / doodles
                    Text(
                        text = "✨💖",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Form items matching mockup Screen 2
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Title field
                    OutlinedTextField(
                        value = memoryTitle,
                        onValueChange = { memoryTitle = it },
                        label = { Text("Title (e.g. First Love)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = palette.primary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_title_input"),
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
                            .testTag("onboarding_date_picker"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Date",
                                    tint = palette.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = memoryDate,
                                    color = palette.textPrimary,
                                    fontSize = 15.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = palette.textSecondary
                            )
                        }
                    }

                    // Add Image field
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("onboarding_add_image"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = "Image",
                                    tint = palette.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (memoryImageUri != null) "Photo selected ✓" else "Add Image (optional)",
                                    color = palette.textPrimary,
                                    fontSize = 15.sp
                                )
                            }
                            if (memoryImageUri != null) {
                                AsyncImage(
                                    model = memoryImageUri,
                                    contentDescription = "Preview",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = palette.textSecondary
                                )
                            }
                        }
                    }

                    // Add short note field
                    OutlinedTextField(
                        value = memoryNote,
                        onValueChange = { memoryNote = it },
                        label = { Text("Add a short note (optional)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = palette.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_note_input"),
                        shape = RoundedCornerShape(16.dp),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.primary,
                            unfocusedBorderColor = palette.surface
                        )
                    )
                }
            } else {
                // Step 2: Personal profile introduction
                Text(
                    text = "Tell us about\nyourself ✨",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Personalize your memory timeline and profile.",
                    fontSize = 14.sp,
                    color = palette.textSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Preferred Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.primary,
                        unfocusedBorderColor = palette.surface
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = userBio,
                    onValueChange = { userBio = it },
                    label = { Text("Personal Quote / Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.primary,
                        unfocusedBorderColor = palette.surface
                    )
                )
            }
        }

        // Bottom Actions: Skip & Next ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    scope.launch {
                        memoryRepository.seedInitialShowcaseMemoriesIfEmpty()
                        preferencesDataStore.setOnboardingDone(true)
                        onComplete()
                    }
                }
            ) {
                Text(
                    text = "Skip",
                    color = palette.textSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = {
                    if (step == 1) {
                        step = 2
                    } else {
                        scope.launch {
                            // Save starter memory if filled
                            if (memoryTitle.isNotBlank()) {
                                memoryRepository.saveMemory(
                                    Memory(
                                        title = memoryTitle,
                                        date = memoryDate,
                                        category = "Love",
                                        description = memoryNote,
                                        imageUri = memoryImageUri,
                                        reminderEnabled = true,
                                        yearlyAnniversary = true
                                    )
                                )
                            }
                            // Seed remaining showcase items so app is rich and interactive
                            memoryRepository.seedInitialShowcaseMemoriesIfEmpty()
                            preferencesDataStore.updateUserProfile(
                                name = userName,
                                email = "rahul.shah@gmail.com",
                                bio = userBio,
                                location = "Kathmandu, Nepal",
                                age = "28 years",
                                gender = "Male"
                            )
                            preferencesDataStore.setOnboardingDone(true)
                            Toast.makeText(context, "Your story starts here! ✨", Toast.LENGTH_SHORT).show()
                            onComplete()
                        }
                    }
                },
                modifier = Modifier
                    .height(50.dp)
                    .width(130.dp)
                    .testTag("onboarding_next_button"),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
            ) {
                Text(
                    text = if (step == 1) "Next →" else "Get Started",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
