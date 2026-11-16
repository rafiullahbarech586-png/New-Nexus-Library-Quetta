package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.LibraryRepository
import com.example.ui.components.LibraryTopBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdmissionFormScreen
import com.example.ui.screens.admin.BookInventoryScreen
import com.example.ui.screens.admin.FeeManagementScreen
import com.example.ui.screens.admin.StudentDetailScreen
import com.example.ui.screens.admin.StudentListScreen
import com.example.ui.screens.student.StudentPortalScreen
import com.example.ui.theme.CardGold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.LibraryViewModelFactory
import com.example.ui.viewmodel.UserRole
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = LibraryRepository(applicationContext)
        val factory = LibraryViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: LibraryViewModel = viewModel(factory = factory)
                LibraryAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LibraryAppRoot(viewModel: LibraryViewModel) {
    val userRole by viewModel.currentUserRole.collectAsState()
    val currentTab by viewModel.currentAdminTab.collectAsState()
    val loggedInStudent by viewModel.loggedInStudent.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    when (userRole) {
        UserRole.NONE -> {
            AuthScreen(viewModel = viewModel)
        }

        UserRole.STUDENT -> {
            BackHandler {
                viewModel.logout()
            }
            loggedInStudent?.let { student ->
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    StudentPortalScreen(
                        student = student,
                        viewModel = viewModel,
                        onLogout = { viewModel.logout() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            } ?: run {
                viewModel.logout()
            }
        }

        UserRole.ADMIN -> {
            if (selectedStudent != null) {
                BackHandler {
                    viewModel.selectStudent(null)
                }
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    StudentDetailScreen(
                        student = selectedStudent!!,
                        viewModel = viewModel,
                        onNavigateBack = { viewModel.selectStudent(null) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            } else {
                BackHandler(enabled = currentTab != AdminTab.DASHBOARD) {
                    viewModel.setAdminTab(AdminTab.DASHBOARD)
                }

                Scaffold(
                    topBar = {
                        LibraryTopBar(
                            title = "New NEXUS Library",
                            subtitle = when (currentTab) {
                                AdminTab.DASHBOARD -> "Administration Control"
                                AdminTab.ADMISSION -> "Student Admission Form"
                                AdminTab.STUDENTS -> "Student Directory & Records"
                                AdminTab.FEES -> "Fee Collection & Dues"
                                AdminTab.BOOKS -> "Library Catalog & Notices"
                                AdminTab.NOTICES -> "Notices"
                            },
                            showBackButton = currentTab != AdminTab.DASHBOARD,
                            onBackClick = { viewModel.setAdminTab(AdminTab.DASHBOARD) },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.logout() },
                                    modifier = Modifier.testTag("admin_logout_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Logout",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AdminTab.DASHBOARD,
                                onClick = { viewModel.setAdminTab(AdminTab.DASHBOARD) },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Overview") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NavyPrimary,
                                    indicatorColor = CardGold
                                ),
                                modifier = Modifier.testTag("nav_dashboard")
                            )

                            NavigationBarItem(
                                selected = currentTab == AdminTab.ADMISSION,
                                onClick = { viewModel.setAdminTab(AdminTab.ADMISSION) },
                                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Admission") },
                                label = { Text("Admission") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NavyPrimary,
                                    indicatorColor = CardGold
                                ),
                                modifier = Modifier.testTag("nav_admission")
                            )

                            NavigationBarItem(
                                selected = currentTab == AdminTab.STUDENTS,
                                onClick = { viewModel.setAdminTab(AdminTab.STUDENTS) },
                                icon = { Icon(Icons.Default.Groups, contentDescription = "Students") },
                                label = { Text("Students") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NavyPrimary,
                                    indicatorColor = CardGold
                                ),
                                modifier = Modifier.testTag("nav_students")
                            )

                            NavigationBarItem(
                                selected = currentTab == AdminTab.FEES,
                                onClick = { viewModel.setAdminTab(AdminTab.FEES) },
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Fees") },
                                label = { Text("Fees") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NavyPrimary,
                                    indicatorColor = CardGold
                                ),
                                modifier = Modifier.testTag("nav_fees")
                            )

                            NavigationBarItem(
                                selected = currentTab == AdminTab.BOOKS,
                                onClick = { viewModel.setAdminTab(AdminTab.BOOKS) },
                                icon = { Icon(Icons.Default.Book, contentDescription = "Books") },
                                label = { Text("Library") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NavyPrimary,
                                    indicatorColor = CardGold
                                ),
                                modifier = Modifier.testTag("nav_books")
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "AdminTabAnimation"
                        ) { tab ->
                            when (tab) {
                                AdminTab.DASHBOARD -> AdminDashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToTab = { viewModel.setAdminTab(it) },
                                    onStudentClick = { viewModel.selectStudent(it) }
                                )

                                AdminTab.ADMISSION -> AdmissionFormScreen(
                                    viewModel = viewModel,
                                    onAdmissionSuccess = { newId ->
                                        // Fetch and show newly created student
                                        scope.launch {
                                            val s = viewModel.allStudents.value.firstOrNull { it.id == newId }
                                            if (s != null) {
                                                viewModel.selectStudent(s)
                                            } else {
                                                viewModel.setAdminTab(AdminTab.STUDENTS)
                                            }
                                        }
                                    },
                                    onNavigateBack = { viewModel.setAdminTab(AdminTab.DASHBOARD) }
                                )

                                AdminTab.STUDENTS -> StudentListScreen(
                                    viewModel = viewModel,
                                    onStudentClick = { viewModel.selectStudent(it) },
                                    onNavigateToAdmission = { viewModel.setAdminTab(AdminTab.ADMISSION) }
                                )

                                AdminTab.FEES -> FeeManagementScreen(
                                    viewModel = viewModel,
                                    onStudentClick = { viewModel.selectStudent(it) }
                                )

                                AdminTab.BOOKS, AdminTab.NOTICES -> BookInventoryScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
