package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BookEntity
import com.example.data.model.FeePaymentEntity
import com.example.data.model.LibraryStats
import com.example.data.model.NoticeEntity
import com.example.data.model.StudentEntity
import com.example.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.data.model.ExpiredReceiptStudent
import com.example.data.model.getReceiptDaysExpired
import com.example.data.model.getReceiptExpiryDate
import com.example.data.model.isReceiptExpired
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole {
    NONE,
    ADMIN,
    STUDENT
}

enum class AdminTab {
    DASHBOARD,
    ADMISSION,
    STUDENTS,
    FEES,
    BOOKS,
    NOTICES
}

enum class StudentFilter {
    ALL,
    EXPIRED_RECEIPTS,
    DUE_FEE,
    SCHOLARSHIP,
    FULLY_PAID,
    ACTIVE
}

class LibraryViewModel(
    private val repository: LibraryRepository
) : ViewModel() {

    // Auth & Navigation State
    private val _currentUserRole = MutableStateFlow(UserRole.NONE)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _currentAdminTab = MutableStateFlow(AdminTab.DASHBOARD)
    val currentAdminTab: StateFlow<AdminTab> = _currentAdminTab.asStateFlow()

    val feeManagementTab = MutableStateFlow(0) // 0: Dues, 1: Expired Receipts, 2: Payment Receipts

    private val _loggedInStudent = MutableStateFlow<StudentEntity?>(null)
    val loggedInStudent: StateFlow<StudentEntity?> = _loggedInStudent.asStateFlow()

    private val _selectedStudent = MutableStateFlow<StudentEntity?>(null)
    val selectedStudent: StateFlow<StudentEntity?> = _selectedStudent.asStateFlow()

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow(StudentFilter.ALL)

    // Snackbar / Feedback
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Core Data
    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val libraryStats: StateFlow<LibraryStats> = repository.libraryStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryStats())

    val allPayments: StateFlow<List<FeePaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBooks: StateFlow<List<BookEntity>> = repository.allBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotices: StateFlow<List<NoticeEntity>> = repository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expired Receipts Student List Flow (Receipt older than 1 month / 30 days)
    val expiredReceiptStudents: StateFlow<List<ExpiredReceiptStudent>> = combine(
        allStudents,
        allPayments
    ) { students, payments ->
        val currentTime = System.currentTimeMillis()
        val expiredList = mutableListOf<ExpiredReceiptStudent>()
        for (student in students) {
            if (!student.isActive) continue
            val studentPayments = payments.filter { it.studentId == student.id }
            val latestPayment = studentPayments.maxByOrNull { it.paymentDate }
            val receiptDate = latestPayment?.paymentDate ?: student.admissionDate
            val receiptNumber = latestPayment?.receiptNumber ?: "ADM-${student.regNumber}"
            val expiryDate = getReceiptExpiryDate(receiptDate)
            if (isReceiptExpired(receiptDate, currentTime)) {
                val daysExpired = getReceiptDaysExpired(receiptDate, currentTime)
                expiredList.add(
                    ExpiredReceiptStudent(
                        student = student,
                        latestPayment = latestPayment,
                        receiptNumber = receiptNumber,
                        receiptDate = receiptDate,
                        expiryDate = expiryDate,
                        daysExpired = daysExpired,
                        amountPaid = latestPayment?.amount ?: student.feeSubmitted
                    )
                )
            }
        }
        expiredList.sortedByDescending { it.daysExpired }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Students Flow
    val filteredStudents: StateFlow<List<StudentEntity>> = combine(
        allStudents,
        allPayments,
        searchQuery,
        selectedFilter
    ) { students, payments, query, filter ->
        var list = students

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.fullName.lowercase().contains(q) ||
                        it.fatherName.lowercase().contains(q) ||
                        it.cnicOrBForm.lowercase().contains(q) ||
                        it.regNumber.lowercase().contains(q) ||
                        it.phone.contains(q) ||
                        it.seatNumber.lowercase().contains(q)
            }
        }

        when (filter) {
            StudentFilter.ALL -> list
            StudentFilter.EXPIRED_RECEIPTS -> {
                val currentTime = System.currentTimeMillis()
                list.filter { student ->
                    val studentPayments = payments.filter { it.studentId == student.id }
                    val receiptDate = studentPayments.maxOfOrNull { it.paymentDate } ?: student.admissionDate
                    isReceiptExpired(receiptDate, currentTime)
                }
            }
            StudentFilter.FULLY_PAID -> list.filter { it.feeRemaining <= 0 }
            StudentFilter.DUE_FEE -> list.filter { it.feeRemaining > 0 }
            StudentFilter.SCHOLARSHIP -> list.filter { it.isScholarship }
            StudentFilter.ACTIVE -> list.filter { it.isActive }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateToExpiredReceiptsTab() {
        feeManagementTab.value = 1
        _currentAdminTab.value = AdminTab.FEES
    }

    // Authentication actions
    fun loginAsAdmin(pin: String): Boolean {
        return if (pin.trim() == "@0310") {
            _currentUserRole.value = UserRole.ADMIN
            _currentAdminTab.value = AdminTab.DASHBOARD
            true
        } else {
            false
        }
    }

    fun loginAsStudent(identifier: String, onResult: (Boolean, String) -> Unit) {
        if (identifier.isBlank()) {
            onResult(false, "Please enter your Roll No, CNIC, or Phone number")
            return
        }

        viewModelScope.launch {
            val student = repository.findStudentByIdentifier(identifier.trim())
            if (student != null) {
                _loggedInStudent.value = student
                _currentUserRole.value = UserRole.STUDENT
                onResult(true, "Welcome back, ${student.fullName}!")
            } else {
                onResult(false, "No student record found matching \"$identifier\"")
            }
        }
    }

    fun logout() {
        _currentUserRole.value = UserRole.NONE
        _loggedInStudent.value = null
        _selectedStudent.value = null
        _currentAdminTab.value = AdminTab.DASHBOARD
    }

    fun setAdminTab(tab: AdminTab) {
        _currentAdminTab.value = tab
    }

    fun selectStudent(student: StudentEntity?) {
        _selectedStudent.value = student
    }

    // Refresh logged in student if data changed
    fun refreshLoggedInStudent() {
        val current = _loggedInStudent.value ?: return
        viewModelScope.launch {
            val updated = repository.getStudentByIdSync(current.id)
            if (updated != null) {
                _loggedInStudent.value = updated
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Admission
    fun enrollStudent(
        fullName: String,
        fatherName: String,
        cnicOrBForm: String,
        phone: String,
        address: String,
        photoUri: String?,
        shift: String,
        seatNumber: String,
        totalFee: Double,
        feeSubmitted: Double,
        isScholarship: Boolean,
        scholarshipDetails: String?,
        notes: String?,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.enrollNewStudent(
                fullName = fullName,
                fatherName = fatherName,
                cnicOrBForm = cnicOrBForm,
                phone = phone,
                address = address,
                photoUri = photoUri,
                shift = shift,
                seatNumber = seatNumber,
                totalFee = totalFee,
                feeSubmitted = feeSubmitted,
                isScholarship = isScholarship,
                scholarshipDetails = scholarshipDetails,
                notes = notes
            )
            _toastEvent.emit("Admission successful for $fullName!")
            onSuccess(id)
        }
    }

    // Update
    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _selectedStudent.value = student
            if (_loggedInStudent.value?.id == student.id) {
                _loggedInStudent.value = student
            }
            _toastEvent.emit("Student details updated successfully")
        }
    }

    // Delete
    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            if (_selectedStudent.value?.id == student.id) {
                _selectedStudent.value = null
            }
            _toastEvent.emit("Student record deleted")
        }
    }

    // Fee Payment
    fun submitFeePayment(
        studentId: String,
        amount: Double,
        method: String,
        note: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val success = repository.recordFeePayment(studentId, amount, method, note)
            if (success) {
                _toastEvent.emit("Fee payment of Rs. ${amount.toInt()} recorded!")
                // update current selected student
                val updated = repository.getStudentByIdSync(studentId)
                if (updated != null) {
                    _selectedStudent.value = updated
                    if (_loggedInStudent.value?.id == studentId) {
                        _loggedInStudent.value = updated
                    }
                }
                onSuccess()
            } else {
                _toastEvent.emit("Failed to record fee payment")
            }
        }
    }

    // Books
    fun addBook(title: String, author: String, category: String, isbn: String) {
        viewModelScope.launch {
            repository.addBook(title, author, category, isbn)
            _toastEvent.emit("Book added to library inventory")
        }
    }

    fun toggleBookIssue(book: BookEntity, student: StudentEntity?) {
        viewModelScope.launch {
            if (book.isAvailable && student != null) {
                repository.updateBook(
                    book.copy(
                        isAvailable = false,
                        issuedToStudentId = student.id,
                        issuedToStudentName = student.fullName,
                        issueDate = System.currentTimeMillis()
                    )
                )
                _toastEvent.emit("Book issued to ${student.fullName}")
            } else if (!book.isAvailable) {
                repository.updateBook(
                    book.copy(
                        isAvailable = true,
                        issuedToStudentId = null,
                        issuedToStudentName = null,
                        issueDate = null
                    )
                )
                _toastEvent.emit("Book returned to library shelf")
            }
        }
    }

    // Notices
    fun postNotice(title: String, message: String, importance: String) {
        viewModelScope.launch {
            repository.postNotice(title, message, importance)
            _toastEvent.emit("Notice posted to library board")
        }
    }

    fun deleteNotice(notice: NoticeEntity) {
        viewModelScope.launch {
            repository.deleteNotice(notice)
            _toastEvent.emit("Notice deleted")
        }
    }
}

class LibraryViewModelFactory(
    private val repository: LibraryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            return LibraryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
