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
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.ExpiredReceiptStudent
import com.example.data.model.FeePaymentEntity
import com.example.data.model.StudentEntity
import com.example.data.model.getReceiptDaysExpired
import com.example.data.model.getReceiptDaysRemaining
import com.example.data.model.getReceiptExpiryDate
import com.example.data.model.isReceiptExpired
import com.example.ui.components.FeeReceiptDialog
import com.example.ui.components.FeeStatusBadge
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StudentAvatar
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.CardGold
import com.example.ui.theme.DueRed
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PaidGreen
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun FeeManagementScreen(
    viewModel: LibraryViewModel,
    onStudentClick: (StudentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.libraryStats.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val expiredReceiptStudents by viewModel.expiredReceiptStudents.collectAsState()
    val selectedTab by viewModel.feeManagementTab.collectAsState()

    val dueStudents = allStudents.filter { it.feeRemaining > 0 }
    var selectedPaymentForReceipt by remember { mutableStateOf<Pair<StudentEntity?, FeePaymentEntity>?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Metrics Banner: Total Collected, Due Balances, and Expired Receipts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMetricCard(
                title = "Total Collected",
                value = formatCurrency(stats.totalFeeSubmitted),
                subValue = "${stats.fullyPaidCount} Students Paid",
                icon = Icons.Default.Payments,
                containerColor = PaidGreen.copy(alpha = 0.15f),
                contentColor = PaidGreen,
                modifier = Modifier.weight(1f)
            )

            StatMetricCard(
                title = "Fee Dues Pending",
                value = formatCurrency(stats.totalFeeRemaining),
                subValue = "${dueStudents.size} Students Due",
                icon = Icons.Default.HourglassTop,
                containerColor = DueRed.copy(alpha = 0.15f),
                contentColor = DueRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Expired Receipts Alert / Summary Strip
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.feeManagementTab.value = 1 },
            shape = RoundedCornerShape(12.dp),
            color = if (expiredReceiptStudents.isEmpty()) PaidGreen.copy(alpha = 0.1f) else DueRed.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (expiredReceiptStudents.isEmpty()) Icons.Default.Receipt else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (expiredReceiptStudents.isEmpty()) PaidGreen else DueRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (expiredReceiptStudents.isEmpty()) {
                                "All student receipts are active & valid (within 1 month)"
                            } else {
                                "${expiredReceiptStudents.size} Student Receipt${if (expiredReceiptStudents.size != 1) "s" else ""} Expired (> 1 Month)"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (expiredReceiptStudents.isEmpty()) PaidGreen else DueRed
                            )
                        )
                        Text(
                            text = "Receipts expire in 30 days. Expired students are listed automatically.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (expiredReceiptStudents.isEmpty()) PaidGreen.copy(alpha = 0.2f) else DueRed
                ) {
                    Text(
                        text = "${expiredReceiptStudents.size} Expired",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (expiredReceiptStudents.isEmpty()) PaidGreen else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3-Way Tab Row: Outstanding Dues, Expired Receipts, Payment Ledger
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { viewModel.feeManagementTab.value = 0 },
                text = {
                    Text(
                        "Dues (${dueStudents.size})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { viewModel.feeManagementTab.value = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Expired (${expiredReceiptStudents.size})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (expiredReceiptStudents.isNotEmpty()) DueRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { viewModel.feeManagementTab.value = 2 },
                text = {
                    Text(
                        "Receipts (${allPayments.size})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Defaulters List
                if (dueStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Awesome! All students have cleared their fees.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = PaidGreen, fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(dueStudents, key = { it.id }) { student ->
                            DefaulterCard(
                                student = student,
                                onClick = { onStudentClick(student) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                }
            }

            1 -> {
                // Expired Receipts Tab
                if (expiredReceiptStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = PaidGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Zero Expired Receipts",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PaidGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "All active students have receipts issued within the last 30 days.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DueRed.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassBottom,
                                        contentDescription = null,
                                        tint = DueRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "These receipts were issued over 1 month ago. Tap 'Renew / Collect Fee' to record monthly fee and issue a fresh 1-month receipt.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = DueRed,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        items(expiredReceiptStudents, key = { it.student.id }) { item ->
                            ExpiredReceiptCard(
                                item = item,
                                onRenewClick = { onStudentClick(item.student) },
                                onViewReceiptClick = {
                                    val paymentToShow = item.latestPayment ?: FeePaymentEntity(
                                        id = "adm_${item.student.id}",
                                        studentId = item.student.id,
                                        studentName = item.student.fullName,
                                        amount = item.amountPaid,
                                        paymentDate = item.receiptDate,
                                        paymentMethod = "Cash",
                                        receiptNumber = item.receiptNumber,
                                        note = "Admission Fee (Expired)"
                                    )
                                    selectedPaymentForReceipt = Pair(item.student, paymentToShow)
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                }
            }

            2 -> {
                // Payments Ledger
                if (allPayments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No fee payments registered yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allPayments, key = { it.id }) { payment ->
                            val student = allStudents.firstOrNull { it.id == payment.studentId }
                            PaymentLedgerCard(
                                payment = payment,
                                student = student,
                                onReceiptClick = {
                                    selectedPaymentForReceipt = Pair(student, payment)
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }

    // Receipt Dialog
    selectedPaymentForReceipt?.let { (student, payment) ->
        if (student != null) {
            FeeReceiptDialog(
                student = student,
                payment = payment,
                onDismiss = { selectedPaymentForReceipt = null }
            )
        }
    }
}

@Composable
fun ExpiredReceiptCard(
    item: ExpiredReceiptStudent,
    onRenewClick: () -> Unit,
    onViewReceiptClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(photoUri = item.student.photoUri, name = item.student.fullName, sizeDp = 46)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.student.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DueRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "EXPIRED ${item.daysExpired}d AGO",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = DueRed
                                )
                            )
                        }
                    }
                    Text(
                        text = "Roll: ${item.student.regNumber} • S/D/O: ${item.student.fatherName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "Ph: ${item.student.phone} • CNIC: ${item.student.cnicOrBForm}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expiration details strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DueRed.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Last Receipt: ${item.receiptNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = DueRed
                        )
                    )
                    Text(
                        text = "Issued: ${formatDate(item.receiptDate)} • Expired: ${formatDate(item.expiryDate)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Last Paid: ${formatCurrency(item.amountPaid)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (item.student.feeRemaining > 0) {
                        Text(
                            text = "Due: ${formatCurrency(item.student.feeRemaining)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DueRed
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewReceiptClick,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Expired", fontSize = 11.sp)
                }

                Button(
                    onClick = onRenewClick,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Renew / Collect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DefaulterCard(
    student: StudentEntity,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(photoUri = student.photoUri, name = student.fullName, sizeDp = 44)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = student.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = student.regNumber,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Text(
                        text = "Father: ${student.fatherName} • Ph: ${student.phone}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DueRed.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Remaining Due Amount",
                        style = MaterialTheme.typography.labelSmall.copy(color = DueRed, fontSize = 10.sp)
                    )
                    Text(
                        text = formatCurrency(student.feeRemaining),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DueRed)
                    )
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Collect Fee", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PaymentLedgerCard(
    payment: FeePaymentEntity,
    student: StudentEntity?,
    onReceiptClick: () -> Unit
) {
    val isExpired = isReceiptExpired(payment.paymentDate)
    val daysRemaining = getReceiptDaysRemaining(payment.paymentDate)
    val daysExpired = getReceiptDaysExpired(payment.paymentDate)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = payment.studentName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isExpired) DueRed.copy(alpha = 0.15f) else PaidGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isExpired) "EXPIRED (${daysExpired}d)" else "VALID (${daysRemaining}d)",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpired) DueRed else PaidGreen
                            )
                        )
                    }
                }
                Text(
                    text = "${payment.receiptNumber} • ${formatDate(payment.paymentDate)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "Mode: ${payment.paymentMethod} ${if (!payment.note.isNullOrBlank()) "• ${payment.note}" else ""}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(payment.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PaidGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onReceiptClick,
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Receipt", fontSize = 11.sp)
                }
            }
        }
    }
}
