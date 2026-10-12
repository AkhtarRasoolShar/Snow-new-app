package com.example.model

import java.text.NumberFormat
import java.util.Locale

fun formatPkr(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "Rs. ${formatter.format(amount.toInt())} PKR"
}

enum class Department(val displayName: String) {
    GARMENT_CARE("DRY CLEANING / LAUNDRY"),
    RETAIL("WASHING POWDER AND PRODUCTS")
}

// Sub-categories for Garment Care
enum class GarmentAudience(val displayName: String) {
    GENTS("Gent's"),
    LADIES("Ladies"),
    HOUSEHOLDS("Households")
}

// Service Filter for Garment Care
enum class ServiceType(val displayName: String) {
    DRY_CLEANING("Dry Cleaning"),
    LAUNDRY("Laundry"),
    STEAM_IRONING("Steam Ironing")
}

// Sub-categories for Retail
enum class RetailCategory(val displayName: String) {
    DEALS("Deals"),
    PRODUCTS("Products")
}

enum class ServiceCategory(val displayName: String, val memberDiscount: Int) {
    PREMIUM_PLUS_SERVICE("Premium plus Service (Laundry & Dry Cleaning)", 30),
    REGULAR_SERVICE("Regular Service (Laundry & Dry Cleaning)", 20),
    CURTAIN_CARPET_BLANKET("Curtain, Carpet & Blanket", 30),
    SPOT_CLEANING_POLISHING("Spot Carpet, Sofa & Floor Cleaning / Polishing", 30),
    WASHING_POWDER_RETAIL("Washing Powder, Phenyle & Others Products", 20)
}

data class Product(
    val id: String,
    val name: String,
    val department: Department = Department.GARMENT_CARE,
    val audience: GarmentAudience = GarmentAudience.GENTS,
    val serviceType: ServiceType = ServiceType.DRY_CLEANING,
    val serviceCategory: ServiceCategory = ServiceCategory.REGULAR_SERVICE,
    val retailCategory: RetailCategory = RetailCategory.PRODUCTS,
    val originalPrice: Double, // in PKR
    val description: String = "",
    val badge: String? = null,
    val isRetail: Boolean = false,
    val imageUrl: String? = null
) {
    fun getEffectiveDiscountPercent(isMember: Boolean): Int {
        return if (isMember) serviceCategory.memberDiscount else 0
    }

    fun getMemberPrice(isMember: Boolean): Double {
        val discount = getEffectiveDiscountPercent(isMember)
        return if (discount > 0) {
            ((originalPrice * (1.0 - discount / 100.0)) * 100.0).toLong() / 100.0
        } else {
            originalPrice
        }
    }

    val memberDiscountPercent: Int
        get() = serviceCategory.memberDiscount

    val memberPrice: Double
        get() = getMemberPrice(true)

    val formattedOriginalPrice: String
        get() = formatPkr(originalPrice)

    val formattedMemberPrice: String
        get() = formatPkr(memberPrice)
}

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val itemOriginalTotal: Double
        get() = product.originalPrice * quantity

    val itemMemberTotal: Double
        get() = product.memberPrice * quantity
}
