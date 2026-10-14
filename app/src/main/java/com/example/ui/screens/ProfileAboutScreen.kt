package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.PreferencesDataStore
import com.example.data.repository.AuthRepository
import com.example.data.repository.MemoryRepository
import com.example.ui.components.ScrapbookBackground
import com.example.ui.theme.LocalOctThemePalette
import kotlinx.coroutines.launch

@Composable
fun ProfileAboutScreen(
    authRepository: AuthRepository,
    memoryRepository: MemoryRepository,
    preferencesDataStore: PreferencesDataStore,
    onNavigateToCustomization: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalOctThemePalette.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userName by preferencesDataStore.userNameFlow.collectAsState(initial = "Rahul Shah")
    val userEmail by preferencesDataStore.userEmailFlow.collectAsState(initial = "rahul.shah@gmail.com")
    val userBio by preferencesDataStore.userBioFlow.collectAsState(initial = "Collecting moments, not things.")
    val userLocation by preferencesDataStore.userLocationFlow.collectAsState(initial = "Kathmandu, Nepal")
    val userAge by preferencesDataStore.userAgeFlow.collectAsState(initial = "28")
    val userGender by preferencesDataStore.userGenderFlow.collectAsState(initial = "Male")
    val userPhotoUri by preferencesDataStore.userPhotoUriFlow.collectAsState(initial = null)

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                preferencesDataStore.updateProfilePhoto(uri.toString())
                Toast.makeText(context, "Profile picture updated! ✨", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ScrapbookBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Avatar with Camera badge
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
                    .background(palette.surface)
                    .border(3.dp, palette.primary, CircleShape)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("profile_avatar_image"),
                contentAlignment = Alignment.Center
            ) {
                if (!userPhotoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = userPhotoUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = if (userGender.equals("Female", ignoreCase = true)) "👩‍🎨" else "🧑‍💻",
                        fontSize = 48.sp
                    )
                }

                // Camera edit badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(palette.primary)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Edit photo",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Name, Gender, Age & Bio
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = palette.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$userGender • $userAge years",
                    fontSize = 14.sp,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"$userBio\"",
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = palette.primary
                )
            }

            // User Info Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Email
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = palette.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Email", fontSize = 11.sp, color = palette.textSecondary)
                            Text(userEmail, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textPrimary)
                        }
                    }

                    // Location
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = palette.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Location", fontSize = 11.sp, color = palette.textSecondary)
                            Text(userLocation, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textPrimary)
                        }
                    }

                    // Age & Gender
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = palette.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Gender & Age", fontSize = 11.sp, color = palette.textSecondary)
                            Text("$userGender • $userAge years old", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textPrimary)
                        }
                    }

                    // Joined
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = palette.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Joined", fontSize = 11.sp, color = palette.textSecondary)
                            Text("Oct 15, 2025", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textPrimary)
                        }
                    }
                }
            }

            // Edit Profile Button
            OutlinedButton(
                onClick = { showEditProfileDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(23.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile Details", color = palette.primary, fontWeight = FontWeight.SemiBold)
            }

            // Quick link to Customization & Themes
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onNavigateToCustomization),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = palette.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = palette.primary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Customization & Themes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.textPrimary)
                            Text("Palettes, backgrounds & typography", fontSize = 12.sp, color = palette.textSecondary)
                        }
                    }
                    Text("Change →", color = palette.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // About OCT15 Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About Oct15", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "A simple app to save your special moments and get reminded. Because some moments deserve to be remembered.\n\nYour life, remembered.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = palette.textSecondary
                    )
                }
            }

            // Developer Card: Built with ❤️ by Rahul Shah
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = palette.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(palette.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = palette.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Developer", fontSize = 11.sp, color = palette.textSecondary)
                        Text(
                            text = "Built with ❤️ by Rahul Shah",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text("Full Stack Developer", fontSize = 12.sp, color = palette.textSecondary)
                    }
                }
            }

            // Share Oct15 Button
            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "OCT15 — Your life, remembered")
                        putExtra(Intent.EXTRA_TEXT, "Preserve and relive your most meaningful memories with OCT15! Made with love by Rahul Shah.")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share OCT15"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("share_oct15_button"),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Oct15", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            // Sign out & Account Deletion Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showSignOutDialog = true }) {
                    Text("Sign Out", color = palette.textSecondary, fontSize = 13.sp)
                }

                TextButton(onClick = { showDeleteAccountDialog = true }) {
                    Text("Delete Account", color = Color(0xFFEF4444), fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Detailed Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userName) }
        var editEmail by remember { mutableStateOf(userEmail) }
        var editBio by remember { mutableStateOf(userBio) }
        var editLocation by remember { mutableStateOf(userLocation) }
        var editAge by remember { mutableStateOf(userAge) }
        var editGender by remember { mutableStateOf(if (userGender.equals("Female", ignoreCase = true)) "Female" else "Male") }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = palette.cardBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Edit Profile Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )

                    // Profile Photo update row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(palette.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!userPhotoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = userPhotoUri,
                                    contentDescription = "Current photo",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(if (editGender == "Female") "👩‍🎨" else "🧑‍💻", fontSize = 28.sp)
                            }
                        }

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                        ) {
                            Text("Change Photo", fontSize = 12.sp)
                        }
                    }

                    // Name
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Email
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Location
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Age
                    OutlinedTextField(
                        value = editAge,
                        onValueChange = { editAge = it },
                        label = { Text("Age") },
                        placeholder = { Text("e.g. 28") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Gender Selection: Exactly Male and Female only!
                    Column {
                        Text(
                            text = "Gender (Male or Female only)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = editGender == "Male",
                                onClick = { editGender = "Male" },
                                label = { Text("Male 👨", fontSize = 14.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = palette.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = editGender == "Female",
                                onClick = { editGender = "Female" },
                                label = { Text("Female 👩", fontSize = 14.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = palette.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Bio
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Personal Quote / Bio") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showEditProfileDialog = false }) {
                            Text("Cancel", color = palette.textSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    preferencesDataStore.updateUserProfile(
                                        name = editName.trim(),
                                        email = editEmail.trim(),
                                        bio = editBio.trim(),
                                        location = editLocation.trim(),
                                        age = editAge.trim(),
                                        gender = editGender
                                    )
                                    showEditProfileDialog = false
                                    Toast.makeText(context, "Profile saved successfully! ✨", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                        ) {
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Sign out dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out from OCT15?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authRepository.signOut()
                        showSignOutDialog = false
                        onSignedOut()
                    }
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold, color = palette.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete account dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account & Data?") },
            text = { Text("This will permanently remove your memories and profile from OCT15. This action cannot be reversed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val res = authRepository.deleteAccount()
                            showDeleteAccountDialog = false
                            if (res.isSuccess) {
                                Toast.makeText(context, "Account and data deleted", Toast.LENGTH_SHORT).show()
                                onSignedOut()
                            } else {
                                Toast.makeText(context, "Failed to delete account: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
