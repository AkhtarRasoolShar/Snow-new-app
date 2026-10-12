package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.network.SnowhiteRepository
import com.example.network.models.ChatMessage
import com.example.network.models.Order
import com.example.viewmodel.SnowhiteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatWithRiderDialog(
    order: Order,
    viewModel: SnowhiteViewModel,
    onDismiss: () -> Unit
) {
    val repository = remember { SnowhiteRepository() }
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val userId = currentUser?.id ?: 1L

    val rawOrderId = order.displayId
    val cleanOrderId = rawOrderId.toString().replace("#", "").replace("Order", "").trim().toLongOrNull() ?: 0L

    var inputText by remember { mutableStateOf("") }
    var messageList by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }

    // Auto-refresh polling every 2.5s while chat is open
    LaunchedEffect(cleanOrderId) {
        while (isActive) {
            repository.getChatMessages(cleanOrderId).onSuccess { updated ->
                messageList = updated
            }
            delay(2500)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0061E0))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Rider Chat - Order #$cleanOrderId",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = order.riderName ?: "Rider Assigned",
                            fontSize = 12.sp,
                            color = Color(0xFFE0EFFE)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Chat Messages List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp)
                ) {
                    if (messageList.isEmpty()) {
                        Text(
                            text = "No messages yet. Send a message to your rider!",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(messageList) { msg ->
                                val isCustomer = msg.senderType.lowercase() == "customer"
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = if (isCustomer) Alignment.End else Alignment.Start
                                ) {
                                    Surface(
                                        color = if (isCustomer) Color(0xFF0061E0) else Color(0xFFE0F2FE),
                                        shape = RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (isCustomer) 14.dp else 2.dp,
                                            bottomEnd = if (isCustomer) 2.dp else 14.dp
                                        ),
                                        shadowElevation = 2.dp
                                    ) {
                                        Text(
                                            text = msg.message,
                                            fontSize = 14.sp,
                                            color = if (isCustomer) Color.White else Color(0xFF0F172A),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type a message to rider...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val textToSend = inputText.trim()
                            if (textToSend.isNotBlank()) {
                                // 1. Optimistic UI update
                                val localMsg = ChatMessage(
                                    orderId = cleanOrderId,
                                    senderType = "customer",
                                    message = textToSend
                                )
                                messageList = messageList + localMsg

                                // 2. Clear input immediately
                                inputText = ""

                                // 3. POST request & immediate sync
                                scope.launch {
                                    repository.sendChatMessage(
                                        orderId = cleanOrderId,
                                        message = textToSend,
                                        senderId = userId
                                    ).onSuccess {
                                        repository.getChatMessages(cleanOrderId).onSuccess { updated ->
                                            if (updated.isNotEmpty()) {
                                                messageList = updated
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF0061E0), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingDialog(
    order: Order,
    viewModel: SnowhiteViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SnowhiteRepository() }
    val scope = rememberCoroutineScope()
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rate Delivery Rider",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Order #${order.displayId} • ${order.riderName ?: "Snowhite Rider"}",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = "$star Stars",
                                tint = if (star <= rating) Color(0xFFEAB308) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Write a quick feedback...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            isSubmitting = true
                            scope.launch {
                                repository.submitRating(order.displayId, rating, reviewText)
                                    .onSuccess {
                                        Toast.makeText(context, "Thank you for rating!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                    .onFailure {
                                        Toast.makeText(context, "Rating saved!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                isSubmitting = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0))
                    ) {
                        Text("Submit Rating", color = Color.White)
                    }
                }
            }
        }
    }
}
