package com.example.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class StudentEntity(
    @DocumentId
    val id: String = "",
    val regNumber: String = "",
    val fullName: String = "",
    val fatherName: String = "",
    val cnicOrBForm: String = "",
    val phone: String = "",
    val address: String = "",
    val photoUri: String? = null,
    val shift: String = "Full Day",
    val seatNumber: String = "Open Desk",
    val totalFee: Double = 0.0,
    val feeSubmitted: Double = 0.0,
    val feeRemaining: Double = 0.0,
    val isScholarship: Boolean = false,
    val scholarshipDetails: String? = null,
    val admissionDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val notes: String? = null
)

@IgnoreExtraProperties
data class FeePaymentEntity(
    @DocumentId
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val amount: Double = 0.0,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash",
    val receiptNumber: String = "",
    val note: String? = null
)

@IgnoreExtraProperties
data class BookEntity(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val category: String = "",
    val isbn: String = "",
    val isAvailable: Boolean = true,
    val issuedToStudentId: String? = null,
    val issuedToStudentName: String? = null,
    val issueDate: Long? = null
)

@IgnoreExtraProperties
data class NoticeEntity(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val postedDate: Long = System.currentTimeMillis(),
    val importance: String = "General"
)

const val ONE_MONTH_DAYS = 30L
const val ONE_MONTH_MILLIS = 30L * 24 * 60 * 60 * 1000L

fun getReceiptExpiryDate(paymentDate: Long): Long = paymentDate + ONE_MONTH_MILLIS

fun isReceiptExpired(paymentDate: Long, currentTime: Long = System.currentTimeMillis()): Boolean =
    currentTime > (paymentDate + ONE_MONTH_MILLIS)

fun getReceiptDaysRemaining(paymentDate: Long, currentTime: Long = System.currentTimeMillis()): Long =
    (((paymentDate + ONE_MONTH_MILLIS) - currentTime) / (24 * 60 * 60 * 1000L)).coerceAtLeast(0)

fun getReceiptDaysExpired(paymentDate: Long, currentTime: Long = System.currentTimeMillis()): Long =
    ((currentTime - (paymentDate + ONE_MONTH_MILLIS)) / (24 * 60 * 60 * 1000L)).coerceAtLeast(0)

data class ExpiredReceiptStudent(
    val student: StudentEntity,
    val latestPayment: FeePaymentEntity?,
    val receiptNumber: String,
    val receiptDate: Long,
    val expiryDate: Long,
    val daysExpired: Long,
    val amountPaid: Double
)

data class LibraryStats(
    val totalStudents: Int = 0,
    val activeStudents: Int = 0,
    val scholarshipStudents: Int = 0,
    val totalFeeExpected: Double = 0.0,
    val totalFeeSubmitted: Double = 0.0,
    val totalFeeRemaining: Double = 0.0,
    val fullyPaidCount: Int = 0,
    val partialPaidCount: Int = 0,
    val unpaidCount: Int = 0,
    val expiredReceiptsCount: Int = 0,
    val activeReceiptsCount: Int = 0
)
