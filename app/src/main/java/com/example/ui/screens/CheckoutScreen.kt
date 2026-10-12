package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.SnowhiteViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CheckoutScreen(
    viewModel: SnowhiteViewModel,
    onNavigateToAuth: () -> Unit,
    onOrderPlacedSuccess: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val isSwMember by viewModel.isSwMember.collectAsState()
    val swCardNumber by viewModel.swCardNumber.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            onNavigateToAuth()
        }
    }

    val user = currentUser ?: return

    var name by remember(user) { mutableStateOf(user.name ?: "") }
    var phone by remember(user) { mutableStateOf(user.phone ?: "") }
    var email by remember(user) { mutableStateOf(user.email ?: "") }
    var address by remember { mutableStateOf("Plot 42-C, 26th Street, DHA Phase 6, Karachi") }
    var notes by remember { mutableStateOf("") }
    var inputSwCardNumber by remember { mutableStateOf(swCardNumber ?: "") }
    var selectedPaymentMethod by remember { mutableIntStateOf(0) }

    val karachiTimestamp = remember {
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy, hh:mm a 'PKT'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Karachi")
        sdf.format(Date())
    }

    val subtotal = viewModel.calculateSubtotal()
    val deliveryFee = viewModel.calculateDeliveryFee()
    val grandTotal = viewModel.calculateGrandTotal()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Pickup & Order Summary",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF0061E0),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Booking Timestamp (Karachi PKT):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0061E0)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = karachiTimestamp,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SW Membership Card Verification",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isSwMember) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFDCFCE7), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SW Card ($swCardNumber) Active! Up to 30% discount applied.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                    var isVerifyingCard by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = inputSwCardNumber,
                        onValueChange = { inputSwCardNumber = it },
                        label = { Text("Enter SW Card No (e.g. B0001) or Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isVerifyingCard,
                        trailingIcon = {
                            if (isVerifyingCard) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF0061E0))
                            } else {
                                TextButton(
                                    onClick = {
                                        if (inputSwCardNumber.isBlank()) {
                                            Toast.makeText(context, "Please enter SW Card Number or Phone", Toast.LENGTH_SHORT).show()
                                            return@TextButton
                                        }
                                        isVerifyingCard = true
                                        viewModel.verifySwCardLive(inputSwCardNumber) { success, _, msg ->
                                            isVerifyingCard = false
                                            Toast.makeText(context, msg ?: if (success) "SW Member Verified!" else "Invalid Card Number", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                ) {
                                    Text("Apply", fontWeight = FontWeight.Bold, color = Color(0xFF0061E0))
                                }
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pickup & Contact Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery / Pickup Address in Karachi") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Laundry Instructions (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Payment Method",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(12.dp))

                val methods = listOf(
                    "Cash on Delivery (COD)" to true,
                    "Easypaisa (Mobile Wallet)" to false,
                    "JazzCash (Mobile Wallet)" to false,
                    "Credit / Debit Card" to false
                )

                methods.forEachIndexed { index, (methodName, isAvailable) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isAvailable) {
                                    selectedPaymentMethod = index
                                } else {
                                    Toast
                                        .makeText(context, "$methodName - Coming Soon!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == index,
                            onClick = {
                                if (isAvailable) {
                                    selectedPaymentMethod = index
                                } else {
                                    Toast
                                        .makeText(context, "$methodName - Coming Soon!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0061E0))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = methodName,
                            fontSize = 15.sp,
                            fontWeight = if (selectedPaymentMethod == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (isAvailable) Color(0xFF0F172A) else Color(0xFF94A3B8)
                        )
                        if (!isAvailable) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Coming Soon",
                                fontSize = 11.sp,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (index < methods.size - 1) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Items in Bag (${cartItems.sumOf { it.quantity }})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(12.dp))

                cartItems.forEach { item ->
                    val itemPrice = viewModel.calculateItemPrice(item.product)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.product.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Qty: ${item.quantity} × Rs. ${itemPrice.toInt()} PKR",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = "Rs. ${(itemPrice * item.quantity).toInt()} PKR",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0061E0)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", color = Color(0xFF64748B))
                    Text("Rs. ${subtotal.toInt()} PKR", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Delivery Fee (Karachi)", color = Color(0xFF64748B))
                    Text("Rs. ${deliveryFee.toInt()} PKR", fontWeight = FontWeight.SemiBold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Payable", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("Rs. ${grandTotal.toInt()} PKR", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0061E0))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                val fullAddress = if (notes.isNotBlank()) "$address (Note: $notes)" else address
                viewModel.createOrder(
                    address = fullAddress,
                    timeSlotPkt = karachiTimestamp
                ) { success, msg ->
                    if (success) {
                        Toast.makeText(context, msg ?: "Pickup scheduled!", Toast.LENGTH_LONG).show()
                        onOrderPlacedSuccess()
                    } else {
                        Toast.makeText(context, msg ?: "Failed to place order", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isLoading && cartItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "CONFIRM & SCHEDULE PICKUP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
