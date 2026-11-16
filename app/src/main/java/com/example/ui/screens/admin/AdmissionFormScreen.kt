package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PhotoPickerSection
import com.example.ui.components.formatCurrency
import com.example.ui.theme.CardGold
import com.example.ui.theme.DueRed
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.ScholarshipPurple
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdmissionFormScreen(
    viewModel: LibraryViewModel,
    onAdmissionSuccess: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var photoUri by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var cnicOrBForm by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedShift by remember { mutableStateOf("Full Day (24/7 Access)") }
    var seatNumber by remember { mutableStateOf("Desk A-06") }
    var totalFeeInput by remember { mutableStateOf("3500") }
    var feeSubmittedInput by remember { mutableStateOf("3500") }
    var isScholarship by remember { mutableStateOf(false) }
    var scholarshipDetails by remember { mutableStateOf("Academic Merit Concession (50%)") }
    var notes by remember { mutableStateOf("CSS / Competitive Exams") }

    var shiftDropdownExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var createdStudentId by remember { mutableStateOf<String?>(null) }

    val shifts = listOf(
        "Full Day (24/7 Access)",
        "Morning (8:00 AM - 2:00 PM)",
        "Evening (2:00 PM - 8:00 PM)",
        "Night (8:00 PM - 2:00 AM)"
    )

    val totalFee = totalFeeInput.toDoubleOrNull() ?: 0.0
    val feeSubmitted = feeSubmittedInput.toDoubleOrNull() ?: 0.0
    val feeRemaining by remember(totalFee, feeSubmitted) {
        derivedStateOf { (totalFee - feeSubmitted).coerceAtLeast(0.0) }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = NavyPrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "New Student Admission",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Register student with CNIC, photo, fee & desk",
                        style = MaterialTheme.typography.bodySmall.copy(color = CardGold)
                    )
                }

                OutlinedButton(
                    onClick = {
                        // Demo autofill for rapid testing
                        fullName = "Bilal Tariq"
                        fatherName = "Tariq Mehmood"
                        cnicOrBForm = "35202-4567890-5"
                        phone = "0300-9876543"
                        address = "House 88, Street 5, Quetta"
                        seatNumber = "Desk B-14"
                        totalFeeInput = "3500"
                        feeSubmittedInput = "2000"
                        isScholarship = false
                        notes = "Medical Entry Preparation"
                    },
                    modifier = Modifier.testTag("autofill_demo_button")
                ) {
                    Text("Auto-fill Demo", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }

        // Section 1: Photo
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
                    text = "1. Student Photo / Avatar",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                PhotoPickerSection(
                    currentPhotoUri = photoUri,
                    onPhotoSelected = { photoUri = it }
                )
            }
        }

        // Section 2: Personal Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "2. Student Identification & Contact",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_name")
                )

                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("Father's Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_father_name")
                )

                OutlinedTextField(
                    value = cnicOrBForm,
                    onValueChange = { cnicOrBForm = it },
                    label = { Text("CNIC or B-Form Number *") },
                    placeholder = { Text("e.g. 54400-1234567-1") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_cnic")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact / WhatsApp Number *") },
                    placeholder = { Text("e.g. 0300-1234567") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_phone")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Current Address / Hostel *") },
                    placeholder = { Text("City, Street, House / Hostel Name") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_address")
                )
            }
        }

        // Section 3: Study Slot & Seat
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "3. Shift & Desk Allocation",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                ExposedDropdownMenuBox(
                    expanded = shiftDropdownExpanded,
                    onExpandedChange = { shiftDropdownExpanded = !shiftDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedShift,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Study Shift") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = shiftDropdownExpanded,
                        onDismissRequest = { shiftDropdownExpanded = false }
                    ) {
                        shifts.forEach { shift ->
                            DropdownMenuItem(
                                text = { Text(shift) },
                                onClick = {
                                    selectedShift = shift
                                    shiftDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = seatNumber,
                    onValueChange = { seatNumber = it },
                    label = { Text("Seat / Desk Number") },
                    placeholder = { Text("e.g. Desk A-12 or Locker 05") },
                    leadingIcon = { Icon(Icons.Default.Chair, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_seat_number")
                )
            }
        }

        // Section 4: Fee & Scholarships
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "4. Fee Structure & Scholarship",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = totalFeeInput,
                        onValueChange = { totalFeeInput = it },
                        label = { Text("Total Fee (Rs.) *") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_total_fee")
                    )

                    OutlinedTextField(
                        value = feeSubmittedInput,
                        onValueChange = { feeSubmittedInput = it },
                        label = { Text("Fee Submitted (Rs.)") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_fee_submitted")
                    )
                }

                // Dynamic Live Fee Calculation Box
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (feeRemaining > 0) DueRed.copy(alpha = 0.1f) else PaidGreen.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Fee Remaining (Balance)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (feeRemaining > 0) DueRed else PaidGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = formatCurrency(feeRemaining),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = if (feeRemaining > 0) DueRed else PaidGreen,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }

                        Text(
                            text = if (feeRemaining <= 0) "Fully Paid" else "Pending Due",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (feeRemaining <= 0) PaidGreen else DueRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Scholarship Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Scholarship / Fee Concession",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Mark as merit or need-based scholarship student",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Switch(
                        checked = isScholarship,
                        onCheckedChange = { isScholarship = it },
                        modifier = Modifier.testTag("switch_scholarship")
                    )
                }

                AnimatedVisibility(visible = isScholarship) {
                    OutlinedTextField(
                        value = scholarshipDetails,
                        onValueChange = { scholarshipDetails = it },
                        label = { Text("Scholarship Details / Category") },
                        placeholder = { Text("e.g. 100% Academic Merit, Hafiz-e-Quran, 50% Concession") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = ScholarshipPurple) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Target Exam / Academic Notes") },
                    placeholder = { Text("e.g. CSS, PMS, MDCAT, CA, General Study") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (formError != null) {
            Text(
                text = formError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Submit Button
        Button(
            onClick = {
                if (fullName.isBlank()) {
                    formError = "Please enter student's full name"
                    return@Button
                }
                if (fatherName.isBlank()) {
                    formError = "Please enter father's name"
                    return@Button
                }
                if (cnicOrBForm.isBlank()) {
                    formError = "Please enter CNIC or B-Form number"
                    return@Button
                }
                if (phone.isBlank()) {
                    formError = "Please enter contact number"
                    return@Button
                }
                if (totalFeeInput.isBlank() || totalFee <= 0) {
                    formError = "Please specify a valid total fee amount"
                    return@Button
                }

                formError = null
                viewModel.enrollStudent(
                    fullName = fullName,
                    fatherName = fatherName,
                    cnicOrBForm = cnicOrBForm,
                    phone = phone,
                    address = address.ifBlank { "Not provided" },
                    photoUri = photoUri,
                    shift = selectedShift,
                    seatNumber = seatNumber.ifBlank { "Open Desk" },
                    totalFee = totalFee,
                    feeSubmitted = feeSubmitted,
                    isScholarship = isScholarship,
                    scholarshipDetails = if (isScholarship) scholarshipDetails else null,
                    notes = notes,
                    onSuccess = { newId ->
                        createdStudentId = newId
                        showSuccessDialog = true
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_admission_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Complete Admission & Generate Card",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onNavigateBack()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PaidGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text("Admission Registered Successfully!")
            },
            text = {
                Column {
                    Text("$fullName has been admitted to New NEXUS Library.")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Seat: $seatNumber • Shift: $selectedShift",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Fee Paid: ${formatCurrency(feeSubmitted)} • Remaining: ${formatCurrency(feeRemaining)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        createdStudentId?.let { id ->
                            onAdmissionSuccess(id)
                        } ?: onNavigateBack()
                    },
                    modifier = Modifier.testTag("dialog_view_student_button")
                ) {
                    Text("View Digital Card & Details")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Done")
                }
            }
        )
    }
}
