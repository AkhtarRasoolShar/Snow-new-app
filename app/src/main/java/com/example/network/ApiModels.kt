package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Generic API wrapper for responses from PHP MySQL backend (api/routes.php).
 */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "status") val status: String = "success",
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null
)

/**
 * Category data model returned by get_categories.
 */
@JsonClass(generateAdapter = true)
data class CategoryDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "type") val type: String = "laundry",
    @Json(name = "is_active") val isActive: Int = 1
)

/**
 * Live product model from MySQL database u998982776_snow via get_products.
 */
@JsonClass(generateAdapter = true)
data class ProductDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "category_id") val categoryId: Int? = null,
    @Json(name = "name") val name: String = "",
    @Json(name = "description") val description: String? = null,
    @Json(name = "price") val price: String? = "0.00",
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "is_active") val isActive: Int? = 1,
    @Json(name = "category_name") val categoryName: String? = null,
    @Json(name = "service_type") val serviceType: String? = null
)

/**
 * App Settings & Logo returned by get_settings.
 */
@JsonClass(generateAdapter = true)
data class AppSettingsDto(
    @Json(name = "app_name") val appName: String = "Snowhite",
    @Json(name = "currency") val currency: String = "PKR",
    @Json(name = "delivery_fee") val deliveryFee: String = "150",
    @Json(name = "logo_url") val logoUrl: String? = null,
    @Json(name = "favicon_url") val faviconUrl: String? = null,
    @Json(name = "support_email") val supportEmail: String? = null,
    @Json(name = "whatsapp_number") val whatsappNumber: String? = null
)

/**
 * Live Order model from get_orders.
 */
@JsonClass(generateAdapter = true)
data class RemoteOrderDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "status") val status: String? = "PENDING",
    @Json(name = "initial_offer_price") val initialOfferPrice: Any? = null,
    @Json(name = "pickup_address") val pickupAddress: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "pickup_time_slot") val pickupTimeSlot: String? = null,
    @Json(name = "customer_name") val customerName: String? = null,
    @Json(name = "customer_phone") val customerPhone: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "payment_method") val paymentMethod: String? = null,
    @Json(name = "items") val items: List<RemoteOrderItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class RemoteOrderItemDto(
    @Json(name = "item") val item: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "price") val price: Any? = null,
    @Json(name = "qty") val qty: Any? = null,
    @Json(name = "quantity") val quantity: Any? = null,
    @Json(name = "service_type") val serviceType: String? = null
)

/**
 * Live Order payload sent to routes.php?action=create_order / place_order
 */
@JsonClass(generateAdapter = true)
data class CreateOrderPayload(
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_phone") val customerPhone: String,
    @Json(name = "email") val email: String?,
    @Json(name = "pickup_address") val pickupAddress: String,
    @Json(name = "notes") val notes: String?,
    @Json(name = "initial_offer_price") val initialOfferPrice: Double,
    @Json(name = "status") val status: String = "PENDING",
    @Json(name = "date") val date: String,
    @Json(name = "payment_method") val paymentMethod: String,
    @Json(name = "items") val items: List<CreateOrderItemPayload>
)

@JsonClass(generateAdapter = true)
data class CreateOrderItemPayload(
    @Json(name = "name") val name: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "price") val price: Double,
    @Json(name = "service_type") val serviceType: String
)

/**
 * Authentication / Registration payload.
 */
@JsonClass(generateAdapter = true)
data class LiveAuthPayload(
    @Json(name = "name") val name: String? = null,
    @Json(name = "phone") val phone: String,
    @Json(name = "email") val email: String? = null,
    @Json(name = "password") val password: String = "Snowhite@123",
    @Json(name = "sw_card_number") val swCardNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class LiveAuthResponse(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "token") val token: String? = null
)

/**
 * User data returned by legacy DTOs
 */
@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "user_id") val userId: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "phone") val phone: String = "",
    @Json(name = "email") val email: String? = null,
    @Json(name = "sw_card_number") val swCardNumber: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "is_member") val isMember: Boolean = false
)

/**
 * SW Card verification response
 */
@JsonClass(generateAdapter = true)
data class CardVerificationDto(
    @Json(name = "valid") val valid: Boolean = false,
    @Json(name = "card_number") val cardNumber: String = "",
    @Json(name = "card_holder") val cardHolder: String? = null,
    @Json(name = "tier") val tier: String = "SW Gold Card Member",
    @Json(name = "discount_percent") val discountPercent: Int = 20,
    @Json(name = "free_delivery") val freeDelivery: Boolean = true,
    @Json(name = "status_message") val statusMessage: String? = null
)

/**
 * Order response returned after placement
 */
@JsonClass(generateAdapter = true)
data class OrderResponseDto(
    @Json(name = "order_id") val orderId: String = "",
    @Json(name = "status") val status: String = "Confirmed",
    @Json(name = "message") val message: String? = null,
    @Json(name = "estimated_delivery") val estimatedDelivery: String? = null
)
