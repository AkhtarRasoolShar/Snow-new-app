package com.example.ui.screens

import androidx.activity.compose.BackHandler

import androidx.compose.animation.AnimatedVisibility
import com.example.ui.components.FloatingStickyCartBar
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.ProductDetailDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartItem
import com.example.model.Product
import com.example.model.RetailCategory
import com.example.model.formatPkr
import com.example.ui.theme.SnowhiteBorder
import com.example.ui.theme.SnowhitePrimary
import com.example.ui.theme.SnowhiteTertiary
import com.example.viewmodel.OrderViewModel

/**
 * SCREEN 5B: WASHING POWDER AND PRODUCTS (Ultra-Clean Layout)
 *
 * CRITICAL UI INSTRUCTIONS:
 * - Clean Row layout matching Screen 5A:
 *   - Left: Light blue square with a machine/product icon.
 *   - Middle: ONLY Item Name and below it, the Price in blue ("Rs. X PKR").
 *     NO descriptions, NO "Bestseller" tags, NO extra text.
 *     If SW Member, show original price crossed out and EXACT discounted price in bold green.
 *   - Right: A solid blue "+ ADD" button. `- 1 +` quantity selector logic when added.
 * - FLOATING CART BAR: Sticky black rounded rectangle at bottom above NavBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailProductsScreen(
    viewModel: OrderViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val membership by viewModel.membership.collectAsState()
    val selectedRetailCategory by viewModel.selectedRetailCategory.collectAsState()
    val filteredProducts by viewModel.filteredRetailProducts.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val cartTotals by viewModel.cartTotals.collectAsState()
    var selectedProductForModal by remember { mutableStateOf<Product?>(null) }

    val categories = listOf(RetailCategory.PRODUCTS, RetailCategory.DEALS)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Washing Powder & Products",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("retail_back_button")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // TABS: "Products", "Deals"
                val selectedTabIndex = categories.indexOf(selectedRetailCategory).coerceAtLeast(0)
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = SnowhitePrimary,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                            color = SnowhitePrimary,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEachIndexed { index, cat ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { viewModel.setRetailCategory(cat) },
                            text = {
                                Text(
                                    text = cat.displayName,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (selectedTabIndex == index) SnowhitePrimary else Color(0xFF64748B)
                                )
                            }
                        )
                    }
                }

                // If SW Member: Light green banner "Gold Card Active: [No] - Up to 30% discounts applied"
                if (membership.isMember) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Text(
                            text = "Gold Card Active: ${membership.memberCode} - Up to 30% discounts applied",
                            fontSize = 12.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                // Product List: Clean Row Layout matching Screen 5A
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("retail_product_list"),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = if (cartTotals.totalItemCount > 0) 110.dp else 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val cartItem = cart[product.id]
                        UltraCleanRetailRow(
                            product = product,
                            isMember = membership.isMember,
                            cartItem = cartItem,
                            onClick = { selectedProductForModal = product },
                            onAdd = { viewModel.addToCart(product) },
                            onRemove = { viewModel.removeFromCart(product.id) }
                        )
                    }
                }
            }

            // FLOATING CART BAR: Sticky black rounded rectangle at bottom above NavBar
            AnimatedVisibility(
                visible = cartTotals.totalItemCount > 0,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                FloatingStickyCartBar(
                    cartTotals = cartTotals,
                    onCheckout = onNavigateToCheckout
                )
            }
        }
    }

    // PRODUCT DETAIL POPUP MODAL
    selectedProductForModal?.let { product ->
        val currentQty = cart[product.id]?.quantity ?: 1
        ProductDetailDialog(
            product = product,
            isMember = membership.isMember,
            initialQuantity = currentQty,
            onDismiss = { selectedProductForModal = null },
            onAddToCart = { qty ->
                viewModel.addToCart(product, qty)
            }
        )
    }
}

@Composable
private fun UltraCleanRetailRow(
    product: Product,
    isMember: Boolean,
    cartItem: CartItem?,
    onClick: () -> Unit = {},
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val quantity = cartItem?.quantity ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("retail_card_${product.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Real Product Image box 70.dp with rounded corners
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    com.example.ui.components.SnowhiteNetworkImage(
                        url = product.imageUrl,
                        contentDescription = product.name,
                        productName = product.name,
                        isRetail = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

            // Middle: ONLY Item Name and below it, Price in blue
            // NO descriptions, NO "Bestseller" tags, NO extra text.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price in PKR
                if (isMember && product.memberDiscountPercent > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formatPkr(product.originalPrice),
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = formatPkr(product.memberPrice),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SnowhiteTertiary // Bold green discounted price
                        )
                    }
                } else {
                    Text(
                        text = formatPkr(product.originalPrice),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SnowhitePrimary // Price in blue
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Solid blue "+ ADD" button or `- 1 +` quantity selector
            if (quantity == 0) {
                Button(
                    onClick = onAdd,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("add_btn_${product.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SnowhitePrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(
                        text = "+ ADD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(2.dp)
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("remove_btn_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "$quantity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .testTag("quantity_val_${product.id}")
                    )

                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("increase_btn_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = SnowhitePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        
        // CRITICAL LAYER REQUIREMENT: Visible bottom accent layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(Color(0xFF0061E0))
        )
    }
}
}

