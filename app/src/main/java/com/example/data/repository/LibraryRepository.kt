package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.BookEntity
import com.example.data.model.FeePaymentEntity
import com.example.data.model.LibraryStats
import com.example.data.model.NoticeEntity
import com.example.data.model.StudentEntity
import com.example.data.model.isReceiptExpired
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class LibraryRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth
    private val studentsCollection = db.collection("students")
    private val paymentsCollection = db.collection("fee_payments")
    private val booksCollection = db.collection("books")
    private val noticesCollection = db.collection("notices")

    // Real-time student list synced across all devices
    val allStudents: Flow<List<StudentEntity>> = callbackFlow {
        val subscription = studentsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val students = snapshot?.toObjects(StudentEntity::class.java) ?: emptyList()
            trySend(students.sortedByDescending { it.admissionDate })
        }
        awaitClose { subscription.remove() }
    }

    // Real-time payments synced across all devices
    val allPayments: Flow<List<FeePaymentEntity>> = callbackFlow {
        val subscription = paymentsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val payments = snapshot?.toObjects(FeePaymentEntity::class.java) ?: emptyList()
            trySend(payments.sortedByDescending { it.paymentDate })
        }
        awaitClose { subscription.remove() }
    }

    // Real-time library books synced across all devices
    val allBooks: Flow<List<BookEntity>> = callbackFlow {
        val subscription = booksCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val books = snapshot?.toObjects(BookEntity::class.java) ?: emptyList()
            trySend(books)
        }
        awaitClose { subscription.remove() }
    }

    // Real-time notices
    val allNotices: Flow<List<NoticeEntity>> = callbackFlow {
        val subscription = noticesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val notices = snapshot?.toObjects(NoticeEntity::class.java) ?: emptyList()
            trySend(notices.sortedByDescending { it.postedDate })
        }
        awaitClose { subscription.remove() }
    }

    val libraryStats: Flow<LibraryStats> = combine(allStudents, allPayments) { students, payments ->
        val totalCount = students.size
        val activeCount = students.count { it.isActive }
        val scholarshipCount = students.count { it.isScholarship }
        val totalExpected = students.sumOf { it.totalFee }
        val totalSubmitted = students.sumOf { it.feeSubmitted }
        val totalRemaining = students.sumOf { it.feeRemaining }
        val fullyPaid = students.count { it.feeRemaining <= 0.0 }
        val partialPaid = students.count { it.feeSubmitted > 0.0 && it.feeRemaining > 0.0 }
        val unpaid = students.count { it.feeSubmitted == 0.0 && it.totalFee > 0.0 }

        val expiredCount = students.count { student ->
            if (!student.isActive) return@count false
            val studentPayments = payments.filter { it.studentId == student.id }
            val latestReceiptDate = studentPayments.maxOfOrNull { it.paymentDate } ?: student.admissionDate
            isReceiptExpired(latestReceiptDate)
        }
        val activeReceiptCount = (activeCount - expiredCount).coerceAtLeast(0)

        LibraryStats(
            totalStudents = totalCount,
            activeStudents = activeCount,
            scholarshipStudents = scholarshipCount,
            totalFeeExpected = totalExpected,
            totalFeeSubmitted = totalSubmitted,
            totalFeeRemaining = totalRemaining,
            fullyPaidCount = fullyPaid,
            partialPaidCount = partialPaid,
            unpaidCount = unpaid,
            expiredReceiptsCount = expiredCount,
            activeReceiptsCount = activeReceiptCount
        )
    }

    suspend fun findStudentByIdentifier(identifier: String): StudentEntity? {
        val query = identifier.trim()
        val snapshot = studentsCollection.get().await()
        val students = snapshot.toObjects(StudentEntity::class.java)
        return students.firstOrNull {
            it.regNumber.equals(query, ignoreCase = true) ||
                    it.cnicOrBForm.equals(query, ignoreCase = true) ||
                    it.cnicOrBForm.replace("-", "").equals(query.replace("-", ""), ignoreCase = true) ||
                    it.phone == query
        }
    }

    suspend fun getStudentByIdSync(id: String): StudentEntity? {
        return try {
            val doc = studentsCollection.document(id).get().await()
            doc.toObject(StudentEntity::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun enrollNewStudent(
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
        notes: String?
    ): String {
        val snapshot = studentsCollection.get().await()
        val count = snapshot.size() + 101
        val regNumber = "NNL-$count"
        val remaining = (totalFee - feeSubmitted).coerceAtLeast(0.0)

        val newDocRef = studentsCollection.document()
        val student = StudentEntity(
            id = newDocRef.id,
            regNumber = regNumber,
            fullName = fullName.trim(),
            fatherName = fatherName.trim(),
            cnicOrBForm = cnicOrBForm.trim(),
            phone = phone.trim(),
            address = address.trim(),
            photoUri = photoUri,
            shift = shift,
            seatNumber = seatNumber.ifBlank { "Open Desk" },
            totalFee = totalFee,
            feeSubmitted = feeSubmitted,
            feeRemaining = remaining,
            isScholarship = isScholarship,
            scholarshipDetails = if (isScholarship) scholarshipDetails?.trim() else null,
            admissionDate = System.currentTimeMillis(),
            isActive = true,
            notes = notes?.trim()
        )

        newDocRef.set(student).await()

        if (feeSubmitted > 0) {
            val receiptNo = "RCP-${System.currentTimeMillis() % 100000}"
            val paymentRef = paymentsCollection.document()
            val payment = FeePaymentEntity(
                id = paymentRef.id,
                studentId = newDocRef.id,
                studentName = fullName.trim(),
                amount = feeSubmitted,
                paymentDate = System.currentTimeMillis(),
                paymentMethod = if (isScholarship && feeSubmitted == totalFee) "Scholarship Grant" else "Cash",
                receiptNumber = receiptNo,
                note = "Admission Initial Fee Payment"
            )
            paymentRef.set(payment).await()
        }

        return newDocRef.id
    }

    suspend fun updateStudent(student: StudentEntity) {
        val remaining = (student.totalFee - student.feeSubmitted).coerceAtLeast(0.0)
        studentsCollection.document(student.id).set(student.copy(feeRemaining = remaining)).await()
    }

    suspend fun recordFeePayment(
        studentId: String,
        amount: Double,
        method: String,
        note: String?
    ): Boolean {
        val student = getStudentByIdSync(studentId) ?: return false
        val newSubmitted = student.feeSubmitted + amount
        val newRemaining = (student.totalFee - newSubmitted).coerceAtLeast(0.0)

        studentsCollection.document(studentId).set(
            student.copy(
                feeSubmitted = newSubmitted,
                feeRemaining = newRemaining
            )
        ).await()

        val receiptNumber = "RCP-${System.currentTimeMillis() % 100000}"
        val paymentRef = paymentsCollection.document()
        val payment = FeePaymentEntity(
            id = paymentRef.id,
            studentId = studentId,
            studentName = student.fullName,
            amount = amount,
            paymentDate = System.currentTimeMillis(),
            paymentMethod = method,
            receiptNumber = receiptNumber,
            note = note ?: "Fee Installment"
        )
        paymentRef.set(payment).await()
        return true
    }

    suspend fun deleteStudent(student: StudentEntity) {
        studentsCollection.document(student.id).delete().await()
    }

    suspend fun addBook(title: String, author: String, category: String, isbn: String) {
        val docRef = booksCollection.document()
        docRef.set(
            BookEntity(
                id = docRef.id,
                title = title.trim(),
                author = author.trim(),
                category = category.trim(),
                isbn = isbn.trim(),
                isAvailable = true
            )
        ).await()
    }

    suspend fun updateBook(book: BookEntity) {
        booksCollection.document(book.id).set(book).await()
    }

    suspend fun postNotice(title: String, message: String, importance: String) {
        val docRef = noticesCollection.document()
        docRef.set(
            NoticeEntity(
                id = docRef.id,
                title = title.trim(),
                message = message.trim(),
                postedDate = System.currentTimeMillis(),
                importance = importance
            )
        ).await()
    }

    suspend fun deleteNotice(notice: NoticeEntity) {
        noticesCollection.document(notice.id).delete().await()
    }

    suspend fun seedInitialDataIfEmpty() {
        try {
            val snapshot = studentsCollection.limit(1).get().await()
            if (!snapshot.isEmpty) return // already has data in cloud!

            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            val initialStudents = listOf(
                StudentEntity(
                    id = "student_101",
                    regNumber = "NNL-101",
                    fullName = "Muhammad Usman",
                    fatherName = "Tariq Mahmood",
                    cnicOrBForm = "35201-8765432-1",
                    phone = "0300-1234567",
                    address = "House # 42, Street 3, Gulshan-e-Iqbal",
                    shift = "Full Day",
                    seatNumber = "Desk A-01",
                    totalFee = 4000.0,
                    feeSubmitted = 4000.0,
                    feeRemaining = 0.0,
                    isScholarship = false,
                    scholarshipDetails = null,
                    admissionDate = now - (20 * dayMs),
                    isActive = true,
                    notes = "CSS Aspirant"
                ),
                StudentEntity(
                    id = "student_102",
                    regNumber = "NNL-102",
                    fullName = "Ayesha Fatima",
                    fatherName = "Abdul Rehman",
                    cnicOrBForm = "35202-9876543-2",
                    phone = "0321-9876543",
                    address = "Plot 18, Block B, Model Town",
                    shift = "Morning (8 AM - 2 PM)",
                    seatNumber = "Desk B-05",
                    totalFee = 2500.0,
                    feeSubmitted = 2500.0,
                    feeRemaining = 0.0,
                    isScholarship = true,
                    scholarshipDetails = "Merit Scholarship (100% Free Access)",
                    admissionDate = now - (15 * dayMs),
                    isActive = true,
                    notes = "Medical Entry Test preparation"
                ),
                StudentEntity(
                    id = "student_103",
                    regNumber = "NNL-103",
                    fullName = "Ali Raza Barech",
                    fatherName = "Ghulam Sarwar",
                    cnicOrBForm = "54400-3456789-3",
                    phone = "0333-5551234",
                    address = "Quetta Town, Near University Road",
                    shift = "Evening (2 PM - 8 PM)",
                    seatNumber = "Desk A-12",
                    totalFee = 3500.0,
                    feeSubmitted = 2000.0,
                    feeRemaining = 1500.0,
                    isScholarship = false,
                    scholarshipDetails = null,
                    admissionDate = now - (10 * dayMs),
                    isActive = true,
                    notes = "Engineering student"
                ),
                StudentEntity(
                    id = "student_104",
                    regNumber = "NNL-104",
                    fullName = "Zubair Ahmed Khan",
                    fatherName = "Nazeer Ahmed",
                    cnicOrBForm = "54401-7654321-4",
                    phone = "0345-9876543",
                    address = "Street 14, Sector C, Quetta Cantt",
                    shift = "Night (8 PM - 2 AM)",
                    seatNumber = "Desk C-08",
                    totalFee = 3500.0,
                    feeSubmitted = 3500.0,
                    feeRemaining = 0.0,
                    isScholarship = false,
                    scholarshipDetails = null,
                    admissionDate = now - (42 * dayMs),
                    isActive = true,
                    notes = "Civil Services Aspirant - Monthly Receipt Expired"
                )
            )

            for (s in initialStudents) {
                studentsCollection.document(s.id).set(s).await()
            }

            val initialPayments = listOf(
                FeePaymentEntity(
                    id = "payment_1",
                    studentId = "student_101",
                    studentName = "Muhammad Usman",
                    amount = 4000.0,
                    paymentDate = now - (20 * dayMs),
                    paymentMethod = "Cash",
                    receiptNumber = "RCP-2026-001",
                    note = "Admission + Monthly Fee Paid in Full"
                ),
                FeePaymentEntity(
                    id = "payment_2",
                    studentId = "student_102",
                    studentName = "Ayesha Fatima",
                    amount = 2500.0,
                    paymentDate = now - (15 * dayMs),
                    paymentMethod = "Scholarship Fund",
                    receiptNumber = "RCP-2026-002",
                    note = "Academic Merit Scholarship Voucher"
                ),
                FeePaymentEntity(
                    id = "payment_3",
                    studentId = "student_103",
                    studentName = "Ali Raza Barech",
                    amount = 2000.0,
                    paymentDate = now - (10 * dayMs),
                    paymentMethod = "JazzCash",
                    receiptNumber = "RCP-2026-003",
                    note = "Partial payment installment 1"
                ),
                FeePaymentEntity(
                    id = "payment_4",
                    studentId = "student_104",
                    studentName = "Zubair Ahmed Khan",
                    amount = 3500.0,
                    paymentDate = now - (42 * dayMs),
                    paymentMethod = "Cash",
                    receiptNumber = "RCP-2026-004",
                    note = "Initial Monthly Fee (Expired 12 days ago)"
                )
            )

            for (p in initialPayments) {
                paymentsCollection.document(p.id).set(p).await()
            }

            val initialBooks = listOf(
                BookEntity(
                    id = "book_1",
                    title = "General Knowledge & Current Affairs 2026",
                    author = "Chaudhry Ahmed",
                    category = "CSS / PMS",
                    isbn = "978-969-12345",
                    isAvailable = false,
                    issuedToStudentId = "student_101",
                    issuedToStudentName = "Muhammad Usman",
                    issueDate = now - (12 * dayMs)
                ),
                BookEntity(
                    id = "book_2",
                    title = "High Yield Physics for MDCAT",
                    author = "Dr. M. Akram",
                    category = "Medical Entry",
                    isbn = "978-969-98765",
                    isAvailable = true
                ),
                BookEntity(
                    id = "book_3",
                    title = "English Grammar & Composition",
                    author = "Wren & Martin",
                    category = "Languages",
                    isbn = "978-019-54321",
                    isAvailable = true
                )
            )

            for (b in initialBooks) {
                booksCollection.document(b.id).set(b).await()
            }

            val initialNotices = listOf(
                NoticeEntity(
                    id = "notice_1",
                    title = "Welcome to New NEXUS Library!",
                    message = "Quiet study hours are strictly observed. High-speed Wi-Fi and air-conditioned halls are available 24/7.",
                    postedDate = now - (18 * dayMs),
                    importance = "General"
                ),
                NoticeEntity(
                    id = "notice_2",
                    title = "Monthly Fee Submission Due",
                    message = "All students with pending remaining balances are requested to clear dues by the 10th of every month.",
                    postedDate = now - (3 * dayMs),
                    importance = "Notice"
                )
            )

            for (n in initialNotices) {
                noticesCollection.document(n.id).set(n).await()
            }
        } catch (e: Exception) {
            // Seed failure gracefully handled
        }
    }
}
