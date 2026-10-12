package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Department
import com.example.model.Product
import com.example.model.formatPkr
import com.example.ui.theme.SnowhiteBorder
import com.example.ui.theme.SnowhitePrimary
import com.example.ui.theme.SnowhiteTertiary

/**
 * PRODUCT POPUP MODAL (DETAIL VIEW)
 * Opens when any Product Card is clicked.
 * Pure white background (RoundedCornerShape(24.dp))
 */
@Composable
fun ProductDetailDialog(
    product: Product,
    isMember: Boolean,
    initialQuantity: Int = 1,
    onDismiss: () -> Unit,
    onAddToCart: (quantity: Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(if (initialQuantity > 0) initialQuantity else 1) }
    val effectivePrice = if (isMember && product.memberDiscountPercent > 0) product.memberPrice else product.originalPrice
    val totalPrice = effectivePrice * quantity

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("product_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header with Category Badge and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val categoryLabel = if (product.department == Department.GARMENT_CARE) {
                        "${product.audience.displayName} • ${product.serviceType.displayName}"
                    } else {
                        "Retail • ${product.retailCategory.displayName}"
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Text(
                            text = categoryLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SnowhitePrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("product_detail_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Large Product Picture at the top (220.dp with rounded corners 16.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("product_detail_image_box"),
                    contentAlignment = Alignment.Center
                ) {
                    SnowhiteNetworkImage(
                        url = product.imageUrl,
                        contentDescription = product.name,
                        productName = product.name,
                        isRetail = product.isRetail,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isMember && product.memberDiscountPercent > 0) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Text(
                                text = "${product.memberDiscountPercent}% OFF (SW Gold)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Full Product Title
                Text(
                    text = product.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.testTag("product_detail_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price in PKR
                if (isMember && product.memberDiscountPercent > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatPkr(product.originalPrice),
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = formatPkr(product.memberPrice),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SnowhiteTertiary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "SW Member Price",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = formatPkr(product.originalPrice),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SnowhitePrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detailed Description
                val displayDescription = if (product.description.isNotBlank()) {
                    product.description
                } else if (product.department == Department.GARMENT_CARE) {
                    "Deluxe bespoke conditioning, gentle stain extraction, hypoallergenic care, and professional hand-pressed finish on wooden hanger."
                } else {
                    "High-potency laundry care formulated specifically for fabric preservation, brilliant whites, and long-lasting freshness."
                }

                Text(
                    text = displayDescription,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF475569),
                    modifier = Modifier.testTag("product_detail_description")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action Row: Quantity Selector & Add to Bag Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quantity Selector (- 1 +)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, SnowhiteBorder)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                enabled = quantity > 1,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("modal_quantity_decrement")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = if (quantity > 1) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = "$quantity",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .testTag("modal_quantity_text")
                            )

                            IconButton(
                                onClick = { quantity++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("modal_quantity_increment")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = SnowhitePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Solid Blue "Add to Bag - Rs. X PKR" Button
                    Button(
                        onClick = {
                            onAddToCart(quantity)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("modal_add_to_bag_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SnowhitePrimary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add to Bag • ${formatPkr(totalPrice)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
