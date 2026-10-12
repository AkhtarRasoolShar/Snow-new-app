package com.example.model

enum class OrderStatus(
    val title: String,
    val subtitle: String,
    val progress: Float
) {
    PLACED("Order Placed", "Awaiting rider assignment", 0.15f),
    CONFIRMED("Confirmed", "Rider assigned for doorstep pickup", 0.30f),
    PICKED_UP("Picked Up", "Items received by Snowhite care valet", 0.45f),
    CLEANING("In Cleaning", "Eco-friendly premium dry wash & treatment", 0.65f),
    STEAM_IRONING("Steam Ironing", "Precision steam press & packaging", 0.80f),
    OUT_FOR_DELIVERY("Out for Delivery", "Rider is heading to your address", 0.92f),
    DELIVERED("Delivered", "Delivered fresh & crisp in garment bags", 1.0f)
}

enum class PaymentMethod(val label: String, val subtitle: String) {
    CASH_ON_DELIVERY("Cash on Delivery (COD)", "Pay with cash or card upon delivery"),
    PAY_ONLINE("Pay Online", "Live API Gateway Integration (Stripe / JazzCash / EasyPaisa / Card)")
}

data class Order(
    val id: String,
    val customerName: String,
    val phone: String,
    val email: String = "",
    val address: String,
    val notes: String = "",
    val items: List<CartItem>,
    val subtotal: Double,
    val discount: Double,
    val deliveryFee: Double,
    val total: Double,
    val isMember: Boolean,
    val paymentMethod: PaymentMethod,
    val status: OrderStatus = OrderStatus.CONFIRMED,
    val orderDate: String,
    val pickupSlot: String,
    val riderName: String = "David Chen (Snowhite Valet)",
    val riderPhone: String = "+92 300 1234567"
)
