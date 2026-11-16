package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.ui.components.FeeStatusBadge
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StudentAvatar
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.CardGold
import com.example.ui.theme.DueRed
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.ScholarshipPurple
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.StudentFilter

@Composable
fun AdminDashboardScreen(
    viewModel: LibraryViewModel,
    onNavigateToTab: (AdminTab) -> Unit,
    onStudentClick: (StudentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.libraryStats.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val recentStudents = allStudents.take(4)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Welcome Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Admin Control Hub",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "New NEXUS Library Analytics & Records",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CardGold
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CardGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onNavigateToTab(AdminTab.ADMISSION) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_new_admission_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardGold,
                            contentColor = NavyPrimary
                        )
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New Student Admission",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Key Strength & Fee Metrics Grid
        item {
            Text(
                text = "Library Strength & Financial Overview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Row 1: Total Strength & Scholarships
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = "Total Strength",
                    value = "${stats.totalStudents} Students",
                    subValue = "${stats.activeStudents} Active Desks",
                    icon = Icons.Default.Groups,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Scholarship Students",
                    value = "${stats.scholarshipStudents} Enrolled",
                    subValue = "Free / Concessions",
                    icon = Icons.Default.School,
                    containerColor = ScholarshipPurple.copy(alpha = 0.15f),
                    contentColor = ScholarshipPurple,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Fee Submitted & Fee Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = "Fee Submitted",
                    value = formatCurrency(stats.totalFeeSubmitted),
                    subValue = "${stats.fullyPaidCount} Fully Cleared",
                    icon = Icons.Default.Payments,
                    containerColor = PaidGreen.copy(alpha = 0.15f),
                    contentColor = PaidGreen,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Fee Remaining (Due)",
                    value = formatCurrency(stats.totalFeeRemaining),
                    subValue = "${stats.partialPaidCount + stats.unpaidCount} Pending Balances",
                    icon = Icons.Default.HourglassTop,
                    containerColor = DueRed.copy(alpha = 0.15f),
                    contentColor = DueRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2.5: Receipt Validity & Expiration Tracking (1 Month Rule)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = "Expired Receipts",
                    value = "${stats.expiredReceiptsCount} Students",
                    subValue = if (stats.expiredReceiptsCount > 0) "Over 1 Month Old (Tap to view)" else "All Up-to-Date",
                    icon = Icons.Default.Receipt,
                    containerColor = if (stats.expiredReceiptsCount > 0) DueRed.copy(alpha = 0.15f) else PaidGreen.copy(alpha = 0.15f),
                    contentColor = if (stats.expiredReceiptsCount > 0) DueRed else PaidGreen,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateToExpiredReceiptsTab() }
                )

                StatMetricCard(
                    title = "Active Valid Receipts",
                    value = "${stats.activeReceiptsCount} Students",
                    subValue = "Valid within 30 days",
                    icon = Icons.Default.CheckCircle,
                    containerColor = PaidGreen.copy(alpha = 0.12f),
                    contentColor = PaidGreen,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab(AdminTab.FEES) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Total Gross Expected Fee
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Gross Fee Expected",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = formatCurrency(stats.totalFeeExpected),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Progress Pill
                    val paidPercentage = if (stats.totalFeeExpected > 0) {
                        ((stats.totalFeeSubmitted / stats.totalFeeExpected) * 100).toInt()
                    } else 0

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PaidGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "$paidPercentage% Collected",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PaidGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Quick Navigation Tiles
        item {
            Text(
                text = "Quick Management Shortcuts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ManagementShortcut(
                        title = "Student Roster",
                        desc = "${allStudents.size} Enrolled",
                        icon = Icons.Default.Groups,
                        onClick = { onNavigateToTab(AdminTab.STUDENTS) },
                        modifier = Modifier.weight(1f)
                    )
                    ManagementShortcut(
                        title = "Fee Dues Hub",
                        desc = "Clear Pending",
                        icon = Icons.Default.AccountBalanceWallet,
                        onClick = { onNavigateToTab(AdminTab.FEES) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ManagementShortcut(
                        title = "Expired Receipts",
                        desc = "${stats.expiredReceiptsCount} Overdue (> 30d)",
                        icon = Icons.Default.Receipt,
                        onClick = { viewModel.navigateToExpiredReceiptsTab() },
                        modifier = Modifier.weight(1f)
                    )
                    ManagementShortcut(
                        title = "Library Books",
                        desc = "Issue & Return",
                        icon = Icons.Default.Bookmark,
                        onClick = { onNavigateToTab(AdminTab.BOOKS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recent Admissions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Admissions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "View All (${allStudents.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable { onNavigateToTab(AdminTab.STUDENTS) }
                )
            }
        }

        if (recentStudents.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No students admitted yet.")
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = { onNavigateToTab(AdminTab.ADMISSION) }) {
                            Text("Register First Student")
                        }
                    }
                }
            }
        } else {
            items(recentStudents) { student ->
                StudentSummaryItem(
                    student = student,
                    onClick = { onStudentClick(student) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ManagementShortcut(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StudentSummaryItem(
    student: StudentEntity,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentAvatar(
                photoUri = student.photoUri,
                name = student.fullName,
                sizeDp = 46
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = student.regNumber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Text(
                    text = "S/O: ${student.fatherName} • ${student.seatNumber}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Submitted: ${formatCurrency(student.feeSubmitted)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PaidGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    FeeStatusBadge(
                        feeRemaining = student.feeRemaining,
                        isScholarship = student.isScholarship
                    )
                }
            }
        }
    }
}
