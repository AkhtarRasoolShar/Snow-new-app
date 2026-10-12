package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Department
import com.example.ui.theme.SnowhitePrimary
import com.example.viewmodel.OrderViewModel

/**
 * SCREEN 4: MAIN DEPARTMENTS SCREEN
 *
 * CRITICAL REQUIREMENTS:
 * - Content aligned vertically in the EXACT CENTER of the screen.
 * - TWO separate, distinct, elevated layered Card containers (RoundedCornerShape(24.dp), elevation = 8.dp, border = 1.5.dp Color(0xFFCBD5E1)).
 * - Distinct 6.dp bottom accent layer strip Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFF0061E0))) at the base of each card.
 * - Centered 72.dp rounded icon box (light blue background #EFF6FF).
 * - NO title written above the button.
 * - STRICTLY NO "Explore Department ->".
 * - Large, full-width, 3D-layered solid blue Button (height = 58.dp, RoundedCornerShape(16.dp), 4.dp bottom shadow layer)
 *   where button text is ONLY the department name:
 *   Button 1: "DRY CLEANING / LAUNDRY ->"
 *   Button 2: "WASHING POWDER AND PRODUCTS ->"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentSelectionScreen(
    viewModel: OrderViewModel,
    onNavigateBack: () -> Unit,
    onSelectGarmentCare: () -> Unit,
    onSelectRetail: () -> Unit
) {
    BackHandler { onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Main Departments",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("dept_back_button")
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
        containerColor = Color.White
    ) { innerPadding ->
        // Content aligned in the EXACT CENTER of the screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card 1: DRY CLEANING / LAUNDRY
                DepartmentLayeredCard(
                    buttonText = "DRY CLEANING / LAUNDRY ->",
                    icon = Icons.Default.DryCleaning,
                    testTag = "dept_card_garment_care",
                    buttonTestTag = "btn_dry_cleaning_laundry",
                    onClick = {
                        viewModel.setDepartment(Department.GARMENT_CARE)
                        onSelectGarmentCare()
                    }
                )

                // Generous spacing between cards
                Spacer(modifier = Modifier.height(28.dp))

                // Card 2: WASHING POWDER AND PRODUCTS
                DepartmentLayeredCard(
                    buttonText = "WASHING POWDER AND PRODUCTS ->",
                    icon = Icons.Default.LocalLaundryService,
                    testTag = "dept_card_retail",
                    buttonTestTag = "btn_washing_powder_products",
                    onClick = {
                        viewModel.setDepartment(Department.RETAIL)
                        onSelectRetail()
                    }
                )
            }
        }
    }
}

/**
 * ELEVATED LAYERED DEPARTMENT CARD:
 * - Pure white background, RoundedCornerShape(24.dp), elevation = 8.dp, 1.5.dp border #CBD5E1.
 * - Center a large rounded icon box (72.dp, light blue background #EFF6FF).
 * - Full-width 3D-layered solid blue Button (height = 58.dp, RoundedCornerShape(16.dp), 4.dp bottom shadow).
 * - Distinct 6.dp solid blue (#0061E0) bottom accent layer strip at the base.
 * - ZERO title text above button, ZERO "Explore Department ->".
 */
@Composable
private fun DepartmentLayeredCard(
    buttonText: String,
    icon: ImageVector,
    testTag: String,
    buttonTestTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.5.dp, Color(0xFFCBD5E1))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Centered 72.dp rounded image/icon box (light blue background #EFF6FF)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val photoUrl = if (buttonText.contains("DRY CLEANING")) {
                        "https://images.unsplash.com/photo-1545173168-9f1947eebb7f?w=400&auto=format&fit=crop&q=80"
                    } else {
                        "https://images.unsplash.com/photo-1583947215259-38e31be8751f?w=400&auto=format&fit=crop&q=80"
                    }
                    com.example.ui.components.SnowhiteNetworkImage(
                        url = photoUrl,
                        contentDescription = buttonText,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Large, full-width, 3D-layered solid blue Button (height = 58.dp, RoundedCornerShape(16.dp))
                // with its own 4.dp bottom shadow/border layer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                ) {
                    // 4.dp bottom shadow / depth layer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF004CB3))
                    )

                    // Solid blue Button
                    Button(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag(buttonTestTag),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SnowhitePrimary),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = buttonText,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // CRITICAL BOTTOM ACCENT LAYER STRIP: 6.dp solid blue #0061E0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFF0061E0))
            )
        }
    }
}
