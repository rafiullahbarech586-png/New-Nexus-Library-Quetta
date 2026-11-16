package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeePaymentEntity
import com.example.data.model.StudentEntity
import com.example.ui.components.DigitalLibraryCard
import com.example.ui.components.FeeReceiptDialog
import com.example.ui.components.ReceiptRow
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.CardGold
import com.example.ui.theme.DueRed
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.ScholarshipPurple
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun StudentDetailScreen(
    student: StudentEntity,
    viewModel: LibraryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allPayments by viewModel.allPayments.collectAsState()
    val studentPayments = allPayments.filter { it.studentId == student.id }
    val allBooks by viewModel.allBooks.collectAsState()
    val issuedBooks = allBooks.filter { it.issuedToStudentId == student.id }

    var showFeePaymentDialog by remember { mutableStateOf(false) }
    var selectedPaymentForReceipt by remember { mutableStateOf<FeePaymentEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top navigation bar row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("detail_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Student Profile",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Row {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Student")
                }
                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Student", tint = DueRed)
                }
            }
        }

        // Digital Library Card View
        DigitalLibraryCard(student = student)

        // Fee Overview & Payment Action Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fee Status & Balances",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (student.isScholarship) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ScholarshipPurple.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Scholarship",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ScholarshipPurple,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FeeDetailColumn(title = "Total Fee", amount = student.totalFee, color = MaterialTheme.colorScheme.onSurface)
                    FeeDetailColumn(title = "Fee Submitted", amount = student.feeSubmitted, color = PaidGreen)
                    FeeDetailColumn(
                        title = "Fee Remaining",
                        amount = student.feeRemaining,
                        color = if (student.feeRemaining > 0) DueRed else PaidGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Receipt 1-Month Expiration Status Box
                val latestPayment = studentPayments.maxByOrNull { it.paymentDate }
                val lastReceiptDate = latestPayment?.paymentDate ?: student.admissionDate
                val isExpired = com.example.data.model.isReceiptExpired(lastReceiptDate)
                val expiryDate = com.example.data.model.getReceiptExpiryDate(lastReceiptDate)
                val daysRemaining = com.example.data.model.getReceiptDaysRemaining(lastReceiptDate)
                val daysExpired = com.example.data.model.getReceiptDaysExpired(lastReceiptDate)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isExpired) DueRed.copy(alpha = 0.1f) else PaidGreen.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isExpired) "⚠️ Monthly Fee Receipt EXPIRED" else "✅ Monthly Fee Receipt ACTIVE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpired) DueRed else PaidGreen
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isExpired) DueRed else PaidGreen
                            ) {
                                Text(
                                    text = if (isExpired) "${daysExpired}d Expired" else "${daysRemaining}d Left",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isExpired) {
                                "Receipt expired on ${formatDate(expiryDate)}. Library rules require monthly renewal. Tap below to collect renewed fee."
                            } else {
                                "Receipt valid for 30 days until ${formatDate(expiryDate)}. Student has active membership access."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                if (student.isScholarship && !student.scholarshipDetails.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Scholarship Type: ${student.scholarshipDetails}",
                        style = MaterialTheme.typography.bodySmall.copy(color = ScholarshipPurple)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showFeePaymentDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("collect_fee_payment_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isExpired) "Renew Fee & Issue New Receipt" else "Record / Submit Fee Payment", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Student Info Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Personal & Academic Information",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))

                ReceiptRow(label = "Full Name", value = student.fullName, isBold = true)
                ReceiptRow(label = "Father's Name", value = student.fatherName)
                ReceiptRow(label = "CNIC or B-Form", value = student.cnicOrBForm)
                ReceiptRow(label = "Contact Phone", value = student.phone)
                ReceiptRow(label = "Address / Hostel", value = student.address)
                ReceiptRow(label = "Study Shift", value = student.shift)
                ReceiptRow(label = "Desk / Seat", value = student.seatNumber)
                ReceiptRow(label = "Admission Date", value = formatDate(student.admissionDate))
                if (!student.notes.isNullOrBlank()) {
                    ReceiptRow(label = "Target Goal / Notes", value = student.notes)
                }
            }
        }

        // Borrowed Books Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Borrowed Library Books (${issuedBooks.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (issuedBooks.isEmpty()) {
                    Text(
                        text = "No books currently checked out.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                } else {
                    issuedBooks.forEach { book ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(book.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("by ${book.author}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                }
                                OutlinedButton(onClick = { viewModel.toggleBookIssue(book, null) }) {
                                    Text("Return", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fee Payment History / Receipts
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Payment History & Receipts (${studentPayments.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (studentPayments.isEmpty()) {
                    Text(
                        text = "No payment records registered yet.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                } else {
                    studentPayments.forEach { payment ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = formatCurrency(payment.amount),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PaidGreen
                                        )
                                    )
                                    Text(
                                        text = "${payment.paymentMethod} • ${formatDate(payment.paymentDate)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Text(
                                        text = payment.receiptNumber,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                OutlinedButton(
                                    onClick = { selectedPaymentForReceipt = payment },
                                    modifier = Modifier.testTag("view_receipt_button_${payment.id}")
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Receipt", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Submit Fee Dialog
    if (showFeePaymentDialog) {
        var paymentAmount by remember { mutableStateOf(student.feeRemaining.coerceAtLeast(0.0).toInt().toString()) }
        var selectedMethod by remember { mutableStateOf("Cash") }
        var paymentNote by remember { mutableStateOf("Monthly Installment") }
        val paymentMethods = listOf("Cash", "JazzCash", "EasyPaisa", "Bank Transfer", "Scholarship")

        AlertDialog(
            onDismissRequest = { showFeePaymentDialog = false },
            title = {
                Text("Record Fee Payment for ${student.fullName}")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Current Due Balance: ${formatCurrency(student.feeRemaining)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DueRed
                        )
                    )

                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { paymentAmount = it },
                        label = { Text("Payment Amount (Rs.) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fee_payment_amount_input")
                    )

                    Text("Payment Method", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        paymentMethods.forEach { method ->
                            FilterChip(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                label = { Text(method) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = paymentNote,
                        onValueChange = { paymentNote = it },
                        label = { Text("Receipt Note / Remarks") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.submitFeePayment(
                                studentId = student.id,
                                amount = amount,
                                method = selectedMethod,
                                note = paymentNote,
                                onSuccess = {
                                    showFeePaymentDialog = false
                                }
                            )
                        }
                    },
                    modifier = Modifier.testTag("confirm_fee_payment_button")
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeePaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Receipt Dialog
    selectedPaymentForReceipt?.let { payment ->
        FeeReceiptDialog(
            student = student,
            payment = payment,
            onDismiss = { selectedPaymentForReceipt = null }
        )
    }

    // Delete Confirm Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Student Record?") },
            text = { Text("Are you sure you want to remove ${student.fullName} (${student.regNumber}) from New NEXUS Library?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteStudent(student)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Student Dialog
    if (showEditDialog) {
        var editSeat by remember { mutableStateOf(student.seatNumber) }
        var editPhone by remember { mutableStateOf(student.phone) }
        var editAddress by remember { mutableStateOf(student.address) }
        var editTotalFee by remember { mutableStateOf(student.totalFee.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Student Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editSeat,
                        onValueChange = { editSeat = it },
                        label = { Text("Seat / Desk Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Contact Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Address / Hostel") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTotalFee,
                        onValueChange = { editTotalFee = it },
                        label = { Text("Total Fee (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fee = editTotalFee.toDoubleOrNull() ?: student.totalFee
                        viewModel.updateStudent(
                            student.copy(
                                seatNumber = editSeat,
                                phone = editPhone,
                                address = editAddress,
                                totalFee = fee
                            )
                        )
                        showEditDialog = false
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FeeDetailColumn(
    title: String,
    amount: Double,
    color: Color
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}
