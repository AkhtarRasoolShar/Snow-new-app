package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.models.Product
import com.example.ui.components.SnowhiteNetworkImage
import com.example.viewmodel.SnowhiteViewModel

@Composable
fun HomeScreen(
    viewModel: SnowhiteViewModel,
    onNavigateToMembership: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val garmentProducts = products.filter { product ->
        val cat = product.categoryName?.lowercase() ?: ""
        !cat.contains("powder") && !cat.contains("retail")
    }
    val retailProducts = products.filter { product ->
        val cat = product.categoryName?.lowercase() ?: ""
        cat.contains("powder") || cat.contains("retail")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        HeroSectionCard(
            onGetStartedClick = onNavigateToMembership
        )

        if (garmentProducts.isNotEmpty()) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Dry Cleaning & Laundry",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(garmentProducts) { product ->
                        HomeProductCard(
                            product = product,
                            viewModel = viewModel,
                            onProductClick = { viewModel.selectProductForModal(product) }
                        )
                    }
                }
            }
        }

        if (retailProducts.isNotEmpty()) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Washing Powder & Retail",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(retailProducts) { product ->
                        HomeProductCard(
                            product = product,
                            viewModel = viewModel,
                            onProductClick = { viewModel.selectProductForModal(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSectionCard(
    onGetStartedClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF0F7FF), Color(0xFFE0EFFE))
                    )
                )
                .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onGetStartedClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 36.dp, vertical = 18.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "Get Started ->",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFF0061E0))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun HomeProductCard(
    product: Product,
    viewModel: SnowhiteViewModel,
    onProductClick: () -> Unit
) {
    val effectivePrice = viewModel.calculateItemPrice(product)
    val isSwMember by viewModel.isSwMember.collectAsState()

    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable { onProductClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                SnowhiteNetworkImage(
                    url = product.imageUrl,
                    productName = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = product.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isSwMember && effectivePrice < product.numericPrice) {
                        Text(
                            text = "Rs. ${product.price} PKR",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                        Text(
                            text = "Rs. ${effectivePrice.toInt()} PKR (SW)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    } else {
                        Text(
                            text = "Rs. ${product.price} PKR",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0061E0)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.addToCart(product, 1) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = "+ ADD",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
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
