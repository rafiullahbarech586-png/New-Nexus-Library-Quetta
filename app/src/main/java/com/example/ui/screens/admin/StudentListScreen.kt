package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.ui.components.FeeStatusBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.components.formatCurrency
import com.example.ui.theme.CardGold
import com.example.ui.theme.DueRed
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.ScholarshipPurple
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.StudentFilter

@Composable
fun StudentListScreen(
    viewModel: LibraryViewModel,
    onStudentClick: (StudentEntity) -> Unit,
    onNavigateToAdmission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val students by viewModel.filteredStudents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.selectedFilter.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search by Name, CNIC, Roll No, Phone...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_student_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentFilter == StudentFilter.ALL,
                    onClick = { viewModel.selectedFilter.value = StudentFilter.ALL },
                    label = { Text("All (${allStudents.size})") }
                )
                FilterChip(
                    selected = currentFilter == StudentFilter.EXPIRED_RECEIPTS,
                    onClick = { viewModel.selectedFilter.value = StudentFilter.EXPIRED_RECEIPTS },
                    label = {
                        val allPayments by viewModel.allPayments.collectAsState()
                        val expiredCount = allStudents.count { s ->
                            val p = allPayments.filter { it.studentId == s.id }
                            val rDate = p.maxOfOrNull { it.paymentDate } ?: s.admissionDate
                            com.example.data.model.isReceiptExpired(rDate)
                        }
                        Text("Expired Receipts ($expiredCount)")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DueRed.copy(alpha = 0.2f),
                        selectedLabelColor = DueRed
                    )
                )
                FilterChip(
                    selected = currentFilter == StudentFilter.DUE_FEE,
                    onClick = { viewModel.selectedFilter.value = StudentFilter.DUE_FEE },
                    label = { Text("Due Fees (${allStudents.count { it.feeRemaining > 0 }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DueRed.copy(alpha = 0.2f),
                        selectedLabelColor = DueRed
                    )
                )
                FilterChip(
                    selected = currentFilter == StudentFilter.SCHOLARSHIP,
                    onClick = { viewModel.selectedFilter.value = StudentFilter.SCHOLARSHIP },
                    label = { Text("Scholarships (${allStudents.count { it.isScholarship }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ScholarshipPurple.copy(alpha = 0.2f),
                        selectedLabelColor = ScholarshipPurple
                    )
                )
                FilterChip(
                    selected = currentFilter == StudentFilter.FULLY_PAID,
                    onClick = { viewModel.selectedFilter.value = StudentFilter.FULLY_PAID },
                    label = { Text("Fully Paid (${allStudents.count { it.feeRemaining <= 0 }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PaidGreen.copy(alpha = 0.2f),
                        selectedLabelColor = PaidGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student Count Header
            Text(
                text = "Showing ${students.size} student${if (students.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No students match your criteria",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                val allPayments by viewModel.allPayments.collectAsState()
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(students, key = { it.id }) { student ->
                        val p = allPayments.filter { it.studentId == student.id }
                        val rDate = p.maxOfOrNull { it.paymentDate } ?: student.admissionDate
                        val isExpired = com.example.data.model.isReceiptExpired(rDate)
                        StudentDetailCard(
                            student = student,
                            isReceiptExpired = isExpired,
                            onClick = { onStudentClick(student) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Action Button for New Admission
        FloatingActionButton(
            onClick = onNavigateToAdmission,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_new_admission"),
            containerColor = CardGold,
            contentColor = NavyPrimary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Student")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Admission", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StudentDetailCard(
    student: StudentEntity,
    isReceiptExpired: Boolean = false,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_card_${student.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(
                    photoUri = student.photoUri,
                    name = student.fullName,
                    sizeDp = 52
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
                            color = NavyPrimary
                        ) {
                            Text(
                                text = student.regNumber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CardGold,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    Text(
                        text = "Father: ${student.fatherName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "CNIC: ${student.cnicOrBForm}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Tag Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = student.seatNumber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = student.shift.split(" ").first(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    if (isReceiptExpired) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DueRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Receipt Expired",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DueRed,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                FeeStatusBadge(
                    feeRemaining = student.feeRemaining,
                    isScholarship = student.isScholarship
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fee mini strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ${formatCurrency(student.totalFee)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "Paid: ${formatCurrency(student.feeSubmitted)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = PaidGreen, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Due: ${formatCurrency(student.feeRemaining)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (student.feeRemaining > 0) DueRed else PaidGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
