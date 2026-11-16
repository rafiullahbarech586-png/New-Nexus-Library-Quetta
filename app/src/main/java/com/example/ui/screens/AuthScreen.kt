package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.auth.GoogleAuthHelper
import com.example.data.model.StudentEntity
import com.example.ui.theme.CardGold
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PaidGreen
import com.example.ui.viewmodel.LibraryViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@Composable
fun AuthScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isSigningInGoogle by remember { mutableStateOf(false) }
    var googleAuthError by remember { mutableStateOf<String?>(null) }

    var selectedRoleTab by remember { mutableIntStateOf(0) } // 0 = Admin, 1 = Student
    var adminPin by remember { mutableStateOf("") }
    var adminPinError by remember { mutableStateOf<String?>(null) }

    var studentIdentifier by remember { mutableStateOf("") }
    var studentLoginError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        GoogleAuthHelper.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        EmeraldPrimary,
                        Color(0xFF072919),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Brand Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(CardGold)
                    .border(3.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalLibrary,
                    contentDescription = "New NEXUS Library Logo",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "NEW NEXUS LIBRARY",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Online Real-Time Multi-Device Library Cloud",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CardGold,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Online Cloud Sync Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (currentUser != null) Color(0xFF144D29) else Color(0xFF1E3A28)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (currentUser != null) PaidGreen else CardGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (currentUser != null) "Cloud Sync Connected" else "Multi-Device Online Sync",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (currentUser != null) "${currentUser?.email ?: "Online Live"}" else "Connect with Google for real-time sync",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    if (currentUser == null) {
                        Button(
                            onClick = {
                                isSigningInGoogle = true
                                googleAuthError = null
                                GoogleAuthHelper.onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isSigningInGoogle = false
                                        currentUser = Firebase.auth.currentUser
                                    },
                                    onAuthError = { err ->
                                        isSigningInGoogle = false
                                        googleAuthError = err
                                    },
                                    scope = coroutineScope,
                                    onAuthCancelled = {
                                        isSigningInGoogle = false
                                    }
                                )
                            },
                            enabled = !isSigningInGoogle,
                            colors = ButtonDefaults.buttonColors(containerColor = CardGold, contentColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("google_sign_in_button")
                        ) {
                            if (isSigningInGoogle) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = EmeraldPrimary
                                )
                            } else {
                                Text("Sign in with Google", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (googleAuthError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = googleAuthError ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFB4AB), fontSize = 11.sp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Role Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Select Portal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab selector
                    TabRow(
                        selectedTabIndex = selectedRoleTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedRoleTab == 0,
                            onClick = { selectedRoleTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Admin Login", fontWeight = FontWeight.SemiBold)
                                }
                            },
                            modifier = Modifier.testTag("tab_admin_login")
                        )
                        Tab(
                            selected = selectedRoleTab == 1,
                            onClick = { selectedRoleTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.School,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Student Portal", fontWeight = FontWeight.SemiBold)
                                }
                            },
                            modifier = Modifier.testTag("tab_student_login")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedRoleTab == 0) {
                        // Admin Login Form
                        Column {
                            Text(
                                text = "Admin Passcode",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = adminPin,
                                onValueChange = {
                                    adminPin = it
                                    adminPinError = null
                                },
                                placeholder = { Text("Enter secret admin passcode") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        val success = viewModel.loginAsAdmin(adminPin)
                                        if (!success) {
                                            adminPinError = "Incorrect admin passcode"
                                        }
                                    }
                                ),
                                isError = adminPinError != null,
                                supportingText = {
                                    if (adminPinError != null) {
                                        Text(adminPinError ?: "", color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text("Restricted administrator access", color = MaterialTheme.colorScheme.outline)
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_pin_input")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val success = viewModel.loginAsAdmin(adminPin)
                                    if (!success) {
                                        adminPinError = "Incorrect admin passcode"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("admin_login_submit_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Sign In as Administrator", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    } else {
                        // Student Login Form
                        Column {
                            Text(
                                text = "Student Identification",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = studentIdentifier,
                                onValueChange = {
                                    studentIdentifier = it
                                    studentLoginError = null
                                },
                                placeholder = { Text("e.g. NNL-101 or 35201-8765432-1") },
                                leadingIcon = {
                                    Icon(Icons.Default.Badge, contentDescription = null)
                                },
                                supportingText = {
                                    if (studentLoginError != null) {
                                        Text(studentLoginError ?: "", color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text("Enter your Roll / Reg No or CNIC / B-Form", color = MaterialTheme.colorScheme.outline)
                                    }
                                },
                                isError = studentLoginError != null,
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("student_identifier_input")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    viewModel.loginAsStudent(studentIdentifier) { success, msg ->
                                        if (!success) {
                                            studentLoginError = msg
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("student_login_submit_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Access Student Portal", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Quick Demo Student Profiles:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                DemoStudentChip(
                                    name = "Muhammad Usman",
                                    rollNo = "NNL-101",
                                    desc = "Full Day • Fully Paid",
                                    onClick = {
                                        studentIdentifier = "NNL-101"
                                        viewModel.loginAsStudent("NNL-101") { _, _ -> }
                                    }
                                )
                                DemoStudentChip(
                                    name = "Ayesha Fatima",
                                    rollNo = "NNL-102",
                                    desc = "Morning • Merit Scholarship",
                                    onClick = {
                                        studentIdentifier = "NNL-102"
                                        viewModel.loginAsStudent("NNL-102") { _, _ -> }
                                    }
                                )
                                DemoStudentChip(
                                    name = "Ali Raza Barech",
                                    rollNo = "NNL-103",
                                    desc = "Evening • Due: Rs. 1,500",
                                    onClick = {
                                        studentIdentifier = "NNL-103"
                                        viewModel.loginAsStudent("NNL-103") { _, _ -> }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Library highlights footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureBadge(title = "Study Desks", desc = "Air Conditioned")
                FeatureBadge(title = "WiFi 5G", desc = "24/7 Power Backup")
                FeatureBadge(title = "Cloud Sync", desc = "Real-time Multi-Device")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DemoStudentChip(
    name: String,
    rollNo: String,
    desc: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = rollNo,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun FeatureBadge(title: String, desc: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                color = CardGold,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        )
    }
}
