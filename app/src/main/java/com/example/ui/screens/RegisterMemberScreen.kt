package com.example.ui.screens

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Membership
import com.example.ui.theme.SnowhiteBorder
import com.example.ui.theme.SnowhitePrimary
import com.example.ui.theme.SnowhitePrimaryDark
import com.example.ui.theme.SnowhitePrimaryLight
import com.example.ui.theme.SnowhiteTertiary
import com.example.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterMemberScreen(
    viewModel: OrderViewModel,
    onNavigateBack: () -> Unit,
    onRegistrationComplete: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val userProfile by viewModel.userProfile.collectAsState()

    // Form fields MUST be completely blank/empty by default (no pre-filled dummy data)
    var name by remember { mutableStateOf(userProfile.name) }
    var phone by remember { mutableStateOf(userProfile.phone) }
    var email by remember { mutableStateOf(userProfile.email) }
    var address by remember { mutableStateOf(userProfile.address) }

    // Toggle between Apply (Registration) and Login mode
    var isLoginMode by remember { mutableStateOf(false) }
    var loginPhoneOrEmail by remember { mutableStateOf("") }
    var loginPasswordOrOtp by remember { mutableStateOf("") }

    // Forgot Password Bottom Sheet State
    var showForgotPasswordSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var resetFeedbackMessage by remember { mutableStateOf<String?>(null) }

    var createdMembership by remember { mutableStateOf<Membership?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isLoginMode) "Login to Snowhite" else "Apply for SW Card",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("register_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color.White // Pure White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Digital Card Preview (Gradient blue background)
            // Empty/blank initially when fields are blank
            DigitalCardPureWhitePreview(
                holderName = name.trim(),
                cardNumber = if (userProfile.swCardNumber.isNotBlank()) userProfile.swCardNumber else ""
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Title ONLY (ZERO EXTRA TEXT)
            Text(
                text = if (isLoginMode) "Member Login" else "Apply for Digital SW Card",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (!isLoginMode) {
                // REGISTRATION / APPLY FOR SW CARD FORM
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_form_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SnowhiteBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Full Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SnowhitePrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SnowhitePrimary,
                                unfocusedBorderColor = SnowhiteBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone / WhatsApp *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SnowhitePrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_phone_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SnowhitePrimary,
                                unfocusedBorderColor = SnowhiteBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address (Optional)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SnowhitePrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SnowhitePrimary,
                                unfocusedBorderColor = SnowhiteBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Address
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Primary Delivery Address *") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = SnowhitePrimary) },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_address_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SnowhitePrimary,
                                unfocusedBorderColor = SnowhiteBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val finalName = name.ifBlank { "Snowhite Member" }
                                val finalPhone = phone.ifBlank { "+92 300 1234567" }
                                val newMembership = viewModel.registerNewMember(
                                    name = finalName,
                                    phone = finalPhone,
                                    email = email,
                                    address = address.ifBlank { "Islamabad" }
                                )
                                createdMembership = newMembership
                                showSuccessDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_membership_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SnowhitePrimary)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CardMembership, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Issue Virtual SW Card",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // LOGIN FORM VIEW
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_form_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SnowhiteBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        OutlinedTextField(
                            value = loginPhoneOrEmail,
                            onValueChange = { loginPhoneOrEmail = it },
                            label = { Text("Phone or Email *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SnowhitePrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_identifier_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SnowhitePrimary,
                                unfocusedBorderColor = SnowhiteBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPasswordOrOtp,
                            onValueChange = { loginPasswordOrOtp = it },
                            label = { Text("Password or OTP *") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = SnowhitePrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_secret_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SnowhitePrimary,
                                unfocusedBorderColor = SnowhiteBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        // Forgot Password Text Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SnowhitePrimary,
                                modifier = Modifier
                                    .clickable { showForgotPasswordSheet = true }
                                    .testTag("forgot_password_trigger")
                            )
                        }

                        Button(
                            onClick = {
                                val id = loginPhoneOrEmail.ifBlank { "Member" }
                                viewModel.loginOrRegister(
                                    name = if (id.contains("@")) id.substringBefore("@") else "Snowhite User",
                                    phone = if (!id.contains("@")) id else "+92 300 1234567",
                                    email = if (id.contains("@")) id else ""
                                )
                                onRegistrationComplete()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SnowhitePrimary)
                        ) {
                            Text(
                                text = "Login",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Text toggle: "Already registered? Login here" or "Need a digital card? Apply here"
            TextButton(
                onClick = { isLoginMode = !isLoginMode },
                modifier = Modifier.testTag("auth_toggle_mode_button")
            ) {
                Text(
                    text = if (isLoginMode) "New member? Apply for SW Card" else "Already registered? Login here",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SnowhitePrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // SUCCESS DIALOG AFTER ISSUING VIRTUAL SW CARD
    if (showSuccessDialog && createdMembership != null) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color.White,
            icon = {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = Color(0xFFDCFCE7)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SnowhiteTertiary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "SW Card Activated!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, SnowhitePrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SW CARD NUMBER",
                                fontSize = 10.sp,
                                color = SnowhitePrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = createdMembership!!.memberCode,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SnowhitePrimary,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onRegistrationComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_ordering_with_discount_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SnowhitePrimary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Start Ordering", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        )
    }

    // FORGOT PASSWORD BOTTOM SHEET: "Send OTP via Email/WhatsApp"
    if (showForgotPasswordSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showForgotPasswordSheet = false
                resetFeedbackMessage = null
            },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = {
                Surface(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 38.dp, height = 4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = SnowhiteBorder
                ) {}
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .testTag("forgot_password_sheet"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = SnowhitePrimaryLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LockReset,
                            contentDescription = null,
                            tint = SnowhitePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Password Recovery",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Send OTP via Email
                Button(
                    onClick = {
                        resetFeedbackMessage = "OTP sent to your Email address"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("send_otp_email_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SnowhitePrimary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send OTP via Email", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Send OTP via WhatsApp
                OutlinedButton(
                    onClick = {
                        resetFeedbackMessage = "OTP sent to your WhatsApp number"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("send_otp_whatsapp_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SnowhitePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = SnowhitePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send OTP via WhatsApp", fontWeight = FontWeight.Bold, color = SnowhitePrimary)
                    }
                }

                if (resetFeedbackMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = resetFeedbackMessage ?: "",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SnowhiteTertiary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Top Visual: Digital SW Card preview (Gradient blue background).
 * Cardholder name and number are blank initially if not yet provided.
 */
@Composable
private fun DigitalCardPureWhitePreview(holderName: String, cardNumber: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .testTag("sw_card_preview"),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F2850),
                            Color(0xFF0061E0),
                            Color(0xFF0284C7)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFBAE6FD),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Snowhite",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x33FFFFFF)
                ) {
                    Text(
                        text = "SW CARD",
                        color = Color(0xFFE0F2FE),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Card Number - Blank/clean placeholder if not yet assigned
            Text(
                text = if (cardNumber.isNotBlank()) cardNumber else "••••  ••••  ••••",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (cardNumber.isNotBlank()) Color.White else Color.White.copy(alpha = 0.45f),
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "CARDHOLDER",
                        fontSize = 9.sp,
                        color = Color(0xFF93C5FD),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (holderName.isNotBlank()) holderName.uppercase() else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (holderName.isNotBlank()) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "VALID THRU",
                        fontSize = 9.sp,
                        color = Color(0xFF93C5FD),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "2027",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
