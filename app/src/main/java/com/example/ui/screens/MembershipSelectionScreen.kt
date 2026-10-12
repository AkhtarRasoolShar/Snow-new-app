package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.SnowhiteBorder
import com.example.ui.theme.SnowhitePrimary
import com.example.viewmodel.OrderViewModel

/**
 * SCREEN 3: MEMBERSHIP SELECTION (Strictly Minimalist & Image Background)
 *
 * CRITICAL UI INSTRUCTIONS:
 * - Completely clean with NO extra paragraphs, subtitles, or explanations.
 * - Background: Image composable (placeholder for "Folded Clothes") covering the whole screen Modifier.fillMaxSize()
 *   with a bright, semi-transparent white overlay over the image (Color.White.copy(alpha = 0.85f)).
 * - Top Header: A clean, bold title "Snowhite Membership" ONLY.
 * - Three beautifully designed, elegant solid white Card composables stacked vertically.
 *   Inside each card, ONLY put an Icon on the left, the main Title text, and an arrow icon on the right. NO SUBTITLES.
 *   1. "Non Members"
 *   2. "SW Card Member" (When clicked, prompt for Membership Number via a clean dialog).
 *   3. "To become a member"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipSelectionScreen(
    viewModel: OrderViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDepartmentSelect: () -> Unit,
    onNavigateToRegistration: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val membership by viewModel.membership.collectAsState()
    var showMemberCodeDialog by remember { mutableStateOf(false) }
    var inputCode by remember { mutableStateOf(if (membership.isMember) membership.memberCode else "") }
    var codeErrorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("membership_selection_screen")
    ) {
        // Background: Image composable ("Folded Clothes") covering whole screen
        Image(
            painter = painterResource(id = R.drawable.img_folded_clothes_bg),
            contentDescription = "Folded Clothes Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Bright, semi-transparent white overlay over the image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.85f))
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Clean Top Bar with back button
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("membership_back_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, SnowhiteBorder),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Top Header: A clean, bold title "Snowhite Membership" ONLY
                Text(
                    text = "Snowhite Membership",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .testTag("membership_main_title")
                )

                // 1. "Non Members"
                MinimalistMembershipCard(
                    title = "Non Members",
                    icon = Icons.Default.ShoppingBag,
                    testTag = "card_non_member",
                    onClick = {
                        viewModel.setNonMember()
                        onNavigateToDepartmentSelect()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. "SW Card Member"
                MinimalistMembershipCard(
                    title = "SW Card Member",
                    icon = Icons.Default.CardMembership,
                    testTag = "card_sw_member",
                    onClick = {
                        if (membership.isMember) {
                            onNavigateToDepartmentSelect()
                        } else {
                            showMemberCodeDialog = true
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. "To become a member"
                MinimalistMembershipCard(
                    title = "To become a member",
                    icon = Icons.Default.PersonAdd,
                    testTag = "card_become_member",
                    onClick = onNavigateToRegistration
                )
            }
        }
    }

    // Clean dialog for entering membership number
    if (showMemberCodeDialog) {
        AlertDialog(
            onDismissRequest = {
                showMemberCodeDialog = false
                codeErrorMessage = null
            },
            title = {
                Text(
                    text = "Enter SW Card Number",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = {
                            inputCode = it
                            codeErrorMessage = null
                        },
                        placeholder = { Text("e.g. B 2152", color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        isError = codeErrorMessage != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SnowhitePrimary,
                            unfocusedBorderColor = SnowhiteBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sw_card_dialog_input")
                    )

                    if (codeErrorMessage != null) {
                        Text(
                            text = codeErrorMessage ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = inputCode.trim()
                        if (trimmed.isEmpty()) {
                            codeErrorMessage = "Please enter your card number."
                        } else {
                            val success = viewModel.validateMembershipCode(trimmed)
                            if (success) {
                                showMemberCodeDialog = false
                                codeErrorMessage = null
                                onNavigateToDepartmentSelect()
                            } else {
                                codeErrorMessage = "Invalid membership number. Please try again."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SnowhitePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("sw_card_dialog_submit")
                ) {
                    Text("Verify & Continue", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showMemberCodeDialog = false
                        codeErrorMessage = null
                    }
                ) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Elegant solid white Card composable:
 * ONLY an Icon on the left, the main Title text, and an arrow icon on the right.
 * NO SUBTITLES, NO EXTRA TEXT.
 */
@Composable
private fun MinimalistMembershipCard(
    title: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SnowhiteBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F1FC)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = SnowhitePrimary,
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
                tint = SnowhitePrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
