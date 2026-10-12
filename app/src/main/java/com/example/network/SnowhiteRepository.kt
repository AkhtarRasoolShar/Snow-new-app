package com.example.network

import com.example.network.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SnowhiteRepository {
    private val baseUrl = "https://snow.akfasft.com/"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private inline fun <reified T> parseApiResponse(jsonString: String): ApiResponse<T>? {
        return try {
            val type = Types.newParameterizedType(ApiResponse::class.java, T::class.java)
            val adapter = moshi.adapter<ApiResponse<T>>(type)
            adapter.fromJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun <T> parseApiResponseList(jsonString: String, elementClass: Class<T>): ApiResponse<List<T>>? {
        return try {
            val listType = Types.newParameterizedType(List::class.java, elementClass)
            val apiResponseType = Types.newParameterizedType(ApiResponse::class.java, listType)
            val adapter = moshi.adapter<ApiResponse<List<T>>>(apiResponseType)
            adapter.fromJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun executeGet(endpointUrl: String): String {
        val request = Request.Builder()
            .url(endpointUrl)
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP error ${response.code}: ${response.message}")
        }
        return response.body?.string() ?: throw IOException("Empty response body")
    }

    private fun executePost(endpointUrl: String, jsonBody: String): String {
        val requestBody = jsonBody.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(endpointUrl)
            .post(requestBody)
            .build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP error ${response.code}: ${response.message}")
        }
        return response.body?.string() ?: throw IOException("Empty response body")
    }

    suspend fun getSettings(): Result<GlobalSettings> = withContext(Dispatchers.IO) {
        try {
            val json = executeGet("${baseUrl}api/routes.php?action=get_settings")
            val apiResponse = parseApiResponse<GlobalSettings>(json)
            if (apiResponse?.status == "success" && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to fetch settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val json = executeGet("${baseUrl}api/routes.php?action=get_categories")
            val apiResponse = parseApiResponseList(json, Category::class.java)
            if (apiResponse?.status == "success") {
                val list = apiResponse.data ?: emptyList()
                val activeList = list.filter { it.isActive == 1 }
                Result.success(activeList)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to fetch categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(categoryId: Long? = null): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val url = if (categoryId != null && categoryId > 0) {
                "${baseUrl}api/routes.php?action=get_products&category_id=$categoryId"
            } else {
                "${baseUrl}api/routes.php?action=get_products"
            }
            val json = executeGet(url)
            val apiResponse = parseApiResponseList(json, Product::class.java)
            if (apiResponse?.status == "success") {
                val list = apiResponse.data ?: emptyList()
                val activeList = list.filter { it.activeInt == 1 }
                Result.success(activeList)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to fetch products"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(emailOrPhone: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("email", emailOrPhone)
                put("phone", emailOrPhone)
                put("password", password)
            }.toString()
            val responseJson = executePost("${baseUrl}api/routes.php?action=login", jsonBody)
            val apiResponse = parseApiResponse<User>(responseJson)
            if (apiResponse?.status == "success" && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Invalid email/phone or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, phone: String, email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("name", name)
                put("phone", phone)
                put("email", email)
                put("password", password)
            }.toString()
            val responseJson = executePost("${baseUrl}api/routes.php?action=register", jsonBody)
            val apiResponse = parseApiResponse<User>(responseJson)
            if (apiResponse?.status == "success" && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPasswordRequest(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("email", email)
            }.toString()
            val responseJson = executePost("${baseUrl}api/routes.php?action=forgot_password", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val message = rootObj.optString("message")
            if (status == "success") {
                Result.success(if (message.isNotEmpty()) message else "OTP sent to your email")
            } else {
                Result.failure(Exception(if (message.isNotEmpty()) message else "Failed to request OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPasswordWithOtp(email: String, otp: String, newPassword: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("email", email)
                put("otp", otp)
                put("new_password", newPassword)
            }.toString()
            val responseJson = executePost("${baseUrl}api/routes.php?action=reset_password", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val message = rootObj.optString("message")
            if (status == "success") {
                Result.success(if (message.isNotEmpty()) message else "Password reset successful")
            } else {
                Result.failure(Exception(if (message.isNotEmpty()) message else "Invalid OTP or reset failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrder(
        customerId: Long,
        pickupAddress: String,
        pickupTimeSlot: String,
        totalAmount: Double,
        items: List<CartItem>
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val itemsArray = JSONArray()
            items.forEach { cartItem ->
                val itemObj = JSONObject().apply {
                    put("name", cartItem.product.name)
                    put("quantity", cartItem.quantity)
                    put("price", cartItem.product.numericPrice)
                    put("service_type", cartItem.product.serviceType ?: "Dry Clean")
                }
                itemsArray.put(itemObj)
            }
            val jsonBody = JSONObject().apply {
                put("customer_id", customerId)
                put("pickup_address", pickupAddress)
                put("pickup_time_slot", pickupTimeSlot)
                put("total_amount", totalAmount)
                put("hub_name", "Karachi")
                put("items", itemsArray)
            }.toString()
            val responseJson = executePost("${baseUrl}api/routes.php?action=create_laundry_order", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val message = rootObj.optString("message")
            val orderId = rootObj.optLong("order_id", 0L)
            if (status == "success") {
                Result.success(orderId)
            } else {
                Result.failure(Exception(if (message.isNotEmpty()) message else "Failed to schedule pickup"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrders(customerId: Long?, phone: String?): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = "${baseUrl}api/routes.php?action=get_customer_orders".toHttpUrlOrNull()?.newBuilder()
            if (customerId != null && customerId > 0) {
                urlBuilder?.addQueryParameter("customer_id", customerId.toString())
            }
            if (!phone.isNullOrEmpty()) {
                urlBuilder?.addQueryParameter("phone", phone)
            }
            val url = urlBuilder?.build()?.toString() ?: "${baseUrl}api/routes.php?action=get_customer_orders"
            val json = executeGet(url)
            val apiResponse = parseApiResponseList(json, Order::class.java)
            if (apiResponse?.status == "success") {
                Result.success(apiResponse.data ?: emptyList())
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to fetch orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatMessages(orderId: Any): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        try {
            val cleanOrderId = orderId.toString().replace("#", "").replace("Order", "").trim().toLongOrNull() ?: 0L
            val url = "${baseUrl}api/routes.php?action=get_chat_messages&order_id=$cleanOrderId"
            val json = executeGet(url)
            val apiResponse = parseApiResponseList(json, ChatMessage::class.java)
            if (apiResponse?.status == "success") {
                Result.success(apiResponse.data ?: emptyList())
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to load chat"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendChatMessage(orderId: Any, message: String, senderId: Long = 1): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanOrderId = orderId.toString().replace("#", "").replace("Order", "").trim().toLongOrNull() ?: 0L
            val jsonBody = JSONObject().apply {
                put("order_id", cleanOrderId)
                put("sender_type", "customer")
                put("sender_id", if (senderId > 0) senderId else 1)
                put("message", message)
            }.toString()
            val responseJson = executePost("${baseUrl}api/routes.php?action=send_chat_message", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val messageText = rootObj.optString("message")
            if (status == "success" || rootObj.has("id")) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(if (messageText.isNotEmpty()) messageText else "Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitRating(orderId: Long, rating: Int, review: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("order_id", orderId)
                put("rating", rating)
                put("review", review)
            }.toString()
            val responseJson = executePost("${baseUrl}api/routes.php?action=submit_rating", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val messageText = rootObj.optString("message")
            if (status == "success") {
                Result.success(Unit)
            } else {
                Result.failure(Exception(if (messageText.isNotEmpty()) messageText else "Failed to submit rating"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPromos(): Result<List<PromoCode>> = withContext(Dispatchers.IO) {
        try {
            val json = executeGet("${baseUrl}api/routes.php?action=get_promos")
            val apiResponse = parseApiResponseList(json, PromoCode::class.java)
            if (apiResponse?.status == "success") {
                Result.success(apiResponse.data ?: emptyList())
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to load promos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
