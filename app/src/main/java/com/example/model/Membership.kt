package com.example.model

data class Membership(
    val isMember: Boolean = false,
    val memberCode: String = "",
    val memberName: String = "",
    val tier: String = "SW Gold Card Member",
    val discountPercent: Int = 20, // default display badge
    val freeDelivery: Boolean = true,
    val validityYear: String = "2026-2027"
)
