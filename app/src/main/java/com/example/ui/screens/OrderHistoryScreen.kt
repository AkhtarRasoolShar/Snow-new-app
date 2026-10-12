package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.formatPkr
import com.example.ui.theme.SnowhiteBorder
import com.example.ui.theme.SnowhitePrimary
import com.example.ui.theme.SnowhitePrimaryDark
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.ui.theme.SnowhitePrimaryLight
import com.example.ui.theme.SnowhiteTertiary
import com.example.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    viewModel: OrderViewModel,
    onTrackOrder: (Order) -> Unit,
    onBookNew: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    BackHandler { onNavigateBack() }

    val orders by viewModel.orders.collectAsState()
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    val filteredOrders = when (selectedFilterIndex) {
        1 -> orders.filter { it.status != OrderStatus.DELIVERED }
        2 -> orders.filter { it.status == OrderStatus.DELIVERED }
        else -> orders
    }

    val syncStatus by viewModel.syncStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Order History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("order_history_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.fetchUserOrdersFromServer() },
                        modifier = Modifier.testTag("sync_orders_from_server_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync from Server",
                            tint = SnowhitePrimary
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
        ) {
            // Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilterIndex == 0,
                    onClick = { selectedFilterIndex = 0 },
                    label = { Text("All Orders (${orders.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SnowhitePrimaryLight,
                        selectedLabelColor = SnowhitePrimaryDark,
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, if (selectedFilterIndex == 0) SnowhitePrimary else SnowhiteBorder)
                )
                FilterChip(
                    selected = selectedFilterIndex == 1,
                    onClick = { selectedFilterIndex = 1 },
                    label = { Text("Active") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SnowhitePrimaryLight,
                        selectedLabelColor = SnowhitePrimaryDark,
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, if (selectedFilterIndex == 1) SnowhitePrimary else SnowhiteBorder)
                )
                FilterChip(
                    selected = selectedFilterIndex == 2,
                    onClick = { selectedFilterIndex = 2 },
                    label = { Text("Delivered") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SnowhitePrimaryLight,
                        selectedLabelColor = SnowhitePrimaryDark,
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, if (selectedFilterIndex == 2) SnowhitePrimary else SnowhiteBorder)
                )
            }

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = SnowhitePrimaryLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = SnowhitePrimary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "No Orders in this View",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )

                        Text(
                            text = "Schedule a pickup to have your garments revitalized.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                        )

                        Button(
                            onClick = onBookNew,
                            colors = ButtonDefaults.buttonColors(containerColor = SnowhitePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Book Garment Care", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("order_history_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        PureWhiteOrderHistoryCard(
                            order = order,
                            onTrack = { onTrackOrder(order) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PureWhiteOrderHistoryCard(
    order: Order,
    onTrack: () -> Unit
) {
    val isDelivered = order.status == OrderStatus.DELIVERED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_history_card_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SnowhiteBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.id,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SnowhitePrimaryDark,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = order.orderDate,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDelivered) Color(0xFFDCFCE7) else SnowhitePrimaryLight,
                    border = BorderStroke(
                        1.dp,
                        if (isDelivered) Color(0xFF86EFAC) else SnowhitePrimary
                    )
                ) {
                    Text(
                        text = order.status.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDelivered) Color(0xFF166534) else SnowhitePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(color = SnowhiteBorder, modifier = Modifier.padding(vertical = 10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}x ${item.product.name}",
                            fontSize = 13.sp,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = formatPkr(item.product.originalPrice * item.quantity),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            HorizontalDivider(color = SnowhiteBorder, modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatPkr(order.total),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = order.paymentMethod.label,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                if (!isDelivered) {
                    Button(
                        onClick = onTrack,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SnowhitePrimary),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live Track", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onTrack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp),
                        border = BorderStroke(1.dp, SnowhiteBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text("View Receipt", fontSize = 12.sp, color = Color(0xFF475569))
                    }
                }
            }
        }
    }
}
