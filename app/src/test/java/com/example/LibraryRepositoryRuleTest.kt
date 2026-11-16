package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.repository.LibraryRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LibraryRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun testAuthenticatedStudentEnrollment() = runTest {
        signInTestUser("admin@nexuslibrary.com")
        val repository = LibraryRepository(firestore)

        val docId = repository.enrollNewStudent(
            fullName = "Zain Ali",
            fatherName = "Ali Ahmed",
            cnicOrBForm = "54400-9876543-1",
            phone = "0300-7654321",
            address = "Quetta",
            photoUri = null,
            shift = "Full Day",
            seatNumber = "Desk A-02",
            totalFee = 4000.0,
            feeSubmitted = 2000.0,
            isScholarship = false,
            scholarshipDetails = null,
            notes = "CSS Prep"
        )

        assertNotNull(docId)
        assertTrue(docId.isNotBlank())

        val doc = firestore.collection("students").document(docId).get().await()
        assertTrue(doc.exists())
        assertEquals("Zain Ali", doc.getString("fullName"))
        assertEquals("Ali Ahmed", doc.getString("fatherName"))
        assertEquals(2000.0, doc.getDouble("feeRemaining") ?: 0.0, 0.01)
    }

    @Test
    fun testReceiptExpirationCalculation() = runTest {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000L

        // Payment from 40 days ago (> 30 days) must be expired
        val oldPaymentDate = now - (40 * dayMs)
        assertTrue(com.example.data.model.isReceiptExpired(oldPaymentDate, now))
        assertEquals(10L, com.example.data.model.getReceiptDaysExpired(oldPaymentDate, now))
        assertEquals(0L, com.example.data.model.getReceiptDaysRemaining(oldPaymentDate, now))

        // Recent payment from 5 days ago must be active/valid
        val recentPaymentDate = now - (5 * dayMs)
        assertTrue(!com.example.data.model.isReceiptExpired(recentPaymentDate, now))
        assertEquals(25L, com.example.data.model.getReceiptDaysRemaining(recentPaymentDate, now))
        assertEquals(0L, com.example.data.model.getReceiptDaysExpired(recentPaymentDate, now))
    }

    @Test
    fun testUnauthenticatedStudentReadFails() = runTest {
        auth.signOut()
        try {
            firestore.collection("students").get().await()
            fail("Expected PERMISSION_DENIED for unauthenticated read")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            assertNotNull(firestoreException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }
}
