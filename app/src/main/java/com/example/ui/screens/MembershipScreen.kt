package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SnowhiteNetworkImage
import com.example.viewmodel.SnowhiteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    viewModel: SnowhiteViewModel,
    onBackClick: () -> Unit,
    onNavigateToDepartments: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    var showSwCardDialog by remember { mutableStateOf(false) }
    var swCardInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Snowhite Membership",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Full-screen background image of Folded Clothes
            SnowhiteNetworkImage(
                url = "https://images.unsplash.com/photo-1582735689369-4fe89db7114c?w=800&q=80",
                productName = "Folded Clothes Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Bright white overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.85f))
            )

            // Center THREE vertically stacked solid white elevated layered Card buttons
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Button 1: Non Members
                MembershipButtonCard(
                    icon = Icons.Default.Person,
                    title = "Non Members",
                    onClick = {
                        viewModel.setSwMemberStatus(false)
                        onNavigateToDepartments()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Button 2: SW Card Member
                MembershipButtonCard(
                    icon = Icons.Default.Badge,
                    title = "SW Card Member",
                    onClick = {
                        showSwCardDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Button 3: To become a member
                MembershipButtonCard(
                    icon = Icons.Default.CardMembership,
                    title = "To become a member",
                    onClick = {
                        onNavigateToRegister()
                    }
                )
            }
        }
    }

    if (showSwCardDialog) {
        AlertDialog(
            onDismissRequest = { showSwCardDialog = false },
            title = {
                Text(
                    text = "Enter SW Membership Card",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your SW Card number to activate 20%-30% Gold Card discounts across all services.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = swCardInput,
                        onValueChange = { swCardInput = it },
                        label = { Text("Card Number (e.g. SW-1002)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (swCardInput.isBlank()) {
                            Toast.makeText(context, "Please enter card number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val success = viewModel.verifySwCard(swCardInput)
                        if (success) {
                            Toast.makeText(context, "SW Card Verified! Discounts Activated", Toast.LENGTH_SHORT).show()
                            showSwCardDialog = false
                            onNavigateToDepartments()
                        } else {
                            Toast.makeText(context, "Invalid Card Number (min 4 chars)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0))
                ) {
                    Text("VERIFY & CONTINUE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwCardDialog = false }) {
                    Text("CANCEL", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun MembershipButtonCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = Color(0xFFF0F7FF),
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color(0xFF0061E0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF0061E0),
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Color(0xFF0061E0))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
