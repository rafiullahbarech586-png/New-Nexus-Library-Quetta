package com.example.ui.screens.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookEntity
import com.example.data.model.StudentEntity
import com.example.ui.components.formatDate
import com.example.ui.theme.CardGold
import com.example.ui.theme.DueRed
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PaidGreen
import com.example.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookInventoryScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val books by viewModel.allBooks.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val notices by viewModel.allNotices.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Books, 1: Notices
    var showAddBookDialog by remember { mutableStateOf(false) }
    var showPostNoticeDialog by remember { mutableStateOf(false) }
    var bookToIssue by remember { mutableStateOf<BookEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Book Catalog (${books.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Notice Board (${notices.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // Books List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(books, key = { it.id }) { book ->
                        BookItemCard(
                            book = book,
                            onIssueClick = { bookToIssue = book },
                            onReturnClick = { viewModel.toggleBookIssue(book, null) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            } else {
                // Notices List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notices, key = { it.id }) { notice ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notice.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (notice.importance == "High") DueRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = notice.importance,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = notice.message, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = formatDate(notice.postedDate),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Floating action button
        FloatingActionButton(
            onClick = {
                if (selectedTab == 0) showAddBookDialog = true else showPostNoticeDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = CardGold,
            contentColor = NavyPrimary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (selectedTab == 0) "Add Book" else "Post Notice", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add Book Dialog
    if (showAddBookDialog) {
        var title by remember { mutableStateOf("") }
        var author by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("CSS / Competitive") }
        var isbn by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddBookDialog = false },
            title = { Text("Add Book to Library Catalog") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Book Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Author / Publisher *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category / Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = isbn,
                        onValueChange = { isbn = it },
                        label = { Text("ISBN / Shelf Tag") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addBook(title, author.ifBlank { "Unknown" }, category, isbn)
                            showAddBookDialog = false
                        }
                    }
                ) {
                    Text("Add Book")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBookDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Issue Book Dialog
    bookToIssue?.let { book ->
        var selectedStudent by remember { mutableStateOf<StudentEntity?>(allStudents.firstOrNull()) }
        var studentDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { bookToIssue = null },
            title = { Text("Issue \"${book.title}\"") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select student to check out this book:")

                    ExposedDropdownMenuBox(
                        expanded = studentDropdownExpanded,
                        onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStudent?.let { "${it.fullName} (${it.regNumber})" } ?: "Select Student",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Enrolled Student") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = studentDropdownExpanded,
                            onDismissRequest = { studentDropdownExpanded = false }
                        ) {
                            allStudents.forEach { student ->
                                DropdownMenuItem(
                                    text = { Text("${student.fullName} (${student.regNumber})") },
                                    onClick = {
                                        selectedStudent = student
                                        studentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedStudent?.let { student ->
                            viewModel.toggleBookIssue(book, student)
                            bookToIssue = null
                        }
                    }
                ) {
                    Text("Issue Book")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToIssue = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Post Notice Dialog
    if (showPostNoticeDialog) {
        var noticeTitle by remember { mutableStateOf("") }
        var noticeContent by remember { mutableStateOf("") }
        var noticeImportance by remember { mutableStateOf("General") }

        AlertDialog(
            onDismissRequest = { showPostNoticeDialog = false },
            title = { Text("Post Library Notice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noticeTitle,
                        onValueChange = { noticeTitle = it },
                        label = { Text("Notice Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noticeContent,
                        onValueChange = { noticeContent = it },
                        label = { Text("Announcement Details *") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noticeTitle.isNotBlank()) {
                            viewModel.postNotice(noticeTitle, noticeContent, noticeImportance)
                            showPostNoticeDialog = false
                        }
                    }
                ) {
                    Text("Post Notice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostNoticeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BookItemCard(
    book: BookEntity,
    onIssueClick: () -> Unit,
    onReturnClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (book.isAvailable) PaidGreen.copy(alpha = 0.15f) else DueRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = if (book.isAvailable) PaidGreen else DueRed,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Author: ${book.author} • ${book.category}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                if (!book.isAvailable && !book.issuedToStudentName.isNullOrBlank()) {
                    Text(
                        text = "Issued to: ${book.issuedToStudentName}",
                        style = MaterialTheme.typography.labelSmall.copy(color = DueRed, fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            if (book.isAvailable) {
                Button(
                    onClick = onIssueClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Issue", fontSize = 12.sp)
                }
            } else {
                OutlinedButton(
                    onClick = onReturnClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Return", fontSize = 12.sp)
                }
            }
        }
    }
}
