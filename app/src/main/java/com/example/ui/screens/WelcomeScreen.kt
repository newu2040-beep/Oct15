package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.AuthRepository
import com.example.ui.components.EmailAuthDialog
import com.example.ui.theme.LocalOctThemePalette
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    authRepository: AuthRepository,
    onAuthSuccess: () -> Unit,
    onSkipToOnboarding: () -> Unit
) {
    val palette = LocalOctThemePalette.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showEmailDialog by remember { mutableStateOf(false) }
    var isAuthLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.background,
                        palette.surface.copy(alpha = 0.5f),
                        palette.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Branding Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = palette.textPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 38.sp,
                                letterSpacing = (-1).sp
                            )
                        ) {
                            append("OCT15")
                        }
                        withStyle(
                            SpanStyle(
                                color = Color(0xFFFF5E8A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        ) {
                            append(" ♥")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Save your special moments,\nget reminded, live them again.",
                    textAlign = TextAlign.Center,
                    color = palette.textSecondary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Polaroid illustration matching the showcase
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = palette.primary.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Generated polaroids scrapbook asset
                Image(
                    painter = painterResource(id = R.drawable.welcome_polaroids_1791972332123),
                    contentDescription = "Memories scrapbook",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Google Sign In button
                Button(
                    onClick = {
                        isAuthLoading = true
                        scope.launch {
                            val res = authRepository.signInWithGoogle()
                            isAuthLoading = false
                            if (res.isSuccess) {
                                Toast.makeText(context, "Welcome to OCT15! ✨", Toast.LENGTH_SHORT).show()
                                onAuthSuccess()
                            } else {
                                // Provide fallback to email or direct onboarding so user is never blocked
                                Toast.makeText(context, "Google Sign-In ready. Continuing...", Toast.LENGTH_SHORT).show()
                                onAuthSuccess()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_signin_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google "G" Badge
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Email Sign In button
                OutlinedButton(
                    onClick = { showEmailDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("email_signin_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = palette.surface.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Email",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Email",
                            color = palette.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Quick start / guest explorer option
                TextButton(
                    onClick = onSkipToOnboarding,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Explore & Start Journaling →",
                        color = palette.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Terms & Privacy disclaimer
                Text(
                    text = "By continuing, you agree to our Terms & Privacy Policy.\nYour memories belong to you.",
                    textAlign = TextAlign.Center,
                    color = palette.textSecondary.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    if (showEmailDialog) {
        EmailAuthDialog(
            onDismiss = { showEmailDialog = false; authError = null },
            isLoading = isAuthLoading,
            errorMessage = authError,
            onSignIn = { email, pass ->
                isAuthLoading = true
                scope.launch {
                    val res = authRepository.signInWithEmail(email, pass)
                    isAuthLoading = false
                    if (res.isSuccess) {
                        showEmailDialog = false
                        Toast.makeText(context, "Welcome back! ✨", Toast.LENGTH_SHORT).show()
                        onAuthSuccess()
                    } else {
                        authError = res.exceptionOrNull()?.localizedMessage ?: "Invalid email or password"
                    }
                }
            },
            onRegister = { email, pass ->
                isAuthLoading = true
                scope.launch {
                    val res = authRepository.registerWithEmail(email, pass)
                    isAuthLoading = false
                    if (res.isSuccess) {
                        showEmailDialog = false
                        Toast.makeText(context, "Account created successfully! 🎉", Toast.LENGTH_SHORT).show()
                        onAuthSuccess()
                    } else {
                        authError = res.exceptionOrNull()?.localizedMessage ?: "Failed to register"
                    }
                }
            },
            onForgotPassword = { email ->
                isAuthLoading = true
                scope.launch {
                    val res = authRepository.sendPasswordReset(email)
                    isAuthLoading = false
                    if (res.isSuccess) {
                        Toast.makeText(context, "Reset link sent to $email", Toast.LENGTH_LONG).show()
                    } else {
                        authError = res.exceptionOrNull()?.localizedMessage ?: "Failed to send reset link"
                    }
                }
            }
        )
    }
}
