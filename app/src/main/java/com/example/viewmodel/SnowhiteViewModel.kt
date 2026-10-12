package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.SnowhiteRepository
import com.example.network.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SnowhiteViewModel : ViewModel() {

    private val repository = SnowhiteRepository()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _settings = MutableStateFlow(GlobalSettings())
    val settings: StateFlow<GlobalSettings> = _settings.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _swCardNumber = MutableStateFlow<String?>(null)
    val swCardNumber: StateFlow<String?> = _swCardNumber.asStateFlow()

    private val _isSwMember = MutableStateFlow(false)
    val isSwMember: StateFlow<Boolean> = _isSwMember.asStateFlow()

    private val _selectedProductForModal = MutableStateFlow<Product?>(null)
    val selectedProductForModal: StateFlow<Product?> = _selectedProductForModal.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getSettings().onSuccess { _settings.value = it }
            repository.getCategories().onSuccess { _categories.value = it }
            repository.getProducts().onSuccess { _products.value = it }
            _isLoading.value = false
        }
    }

    fun setSwMemberStatus(isMember: Boolean) {
        _isSwMember.value = isMember
        if (!isMember) {
            _swCardNumber.value = null
        }
    }

    fun verifySwCard(cardNumber: String): Boolean {
        val trimmed = cardNumber.trim()
        if (trimmed.length >= 4) {
            _swCardNumber.value = trimmed
            _isSwMember.value = true
            return true
        } else {
            _swCardNumber.value = null
            _isSwMember.value = false
            return false
        }
    }

    fun selectProductForModal(product: Product?) {
        _selectedProductForModal.value = product
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            val newQty = existing.quantity + quantity
            if (newQty > 0) {
                currentList[index] = existing.copy(quantity = newQty)
            } else {
                currentList.removeAt(index)
            }
        } else if (quantity > 0) {
            currentList.add(CartItem(product, quantity))
        }
        _cartItems.value = currentList
    }

    fun updateCartQuantity(productId: Long, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val existing = currentList[index]
            val newQty = existing.quantity + delta
            if (newQty > 0) {
                currentList[index] = existing.copy(quantity = newQty)
            } else {
                currentList.removeAt(index)
            }
            _cartItems.value = currentList
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getCartQuantity(productId: Long): Int {
        return _cartItems.value.find { it.product.id == productId }?.quantity ?: 0
    }

    fun calculateItemPrice(product: Product): Double {
        val basePrice = product.numericPrice
        if (!_isSwMember.value) return basePrice

        val catName = product.categoryName?.lowercase() ?: ""
        val name = product.name.lowercase()

        val isThirtyPercent = name.contains("premium plus") ||
                catName.contains("household") ||
                name.contains("curtain") ||
                name.contains("blanket") ||
                name.contains("carpet") ||
                name.contains("sofa")

        val discountFraction = if (isThirtyPercent) 0.30 else 0.20
        return basePrice * (1.0 - discountFraction)
    }

    fun calculateSubtotal(): Double {
        return _cartItems.value.sumOf { cartItem ->
            calculateItemPrice(cartItem.product) * cartItem.quantity
        }
    }

    fun calculateDeliveryFee(): Double {
        return _settings.value.deliveryFee?.toDoubleOrNull() ?: 150.0
    }

    fun calculateGrandTotal(): Double {
        val subtotal = calculateSubtotal()
        if (subtotal <= 0) return 0.0
        return subtotal + calculateDeliveryFee()
    }

    fun login(emailOrPhone: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(emailOrPhone, password)
            _isLoading.value = false
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    if (user.isSwMember == true || !user.swCardNumber.isNullOrEmpty()) {
                        _isSwMember.value = true
                        _swCardNumber.value = user.swCardNumber
                    }
                    loadOrders()
                    onResult(true, null)
                },
                onFailure = { error ->
                    onResult(false, error.message ?: "Login failed")
                }
            )
        }
    }

    fun register(name: String, phone: String, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.register(name, phone, email, password)
            _isLoading.value = false
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    loadOrders()
                    onResult(true, null)
                },
                onFailure = { error ->
                    onResult(false, error.message ?: "Registration failed")
                }
            )
        }
    }

    fun loadOrders() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.getOrders(user.id, user.phone ?: user.email)
            res.onSuccess { list ->
                _orders.value = list
            }
        }
    }

    fun createOrder(
        address: String,
        timeSlotPkt: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "Please log in to place an order")
            return
        }
        val items = _cartItems.value
        if (items.isEmpty()) {
            onResult(false, "Cart is empty")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val total = calculateGrandTotal()
            val result = repository.createOrder(
                customerId = user.id,
                pickupAddress = address,
                pickupTimeSlot = timeSlotPkt,
                totalAmount = total,
                items = items
            )
            _isLoading.value = false
            result.fold(
                onSuccess = { orderId ->
                    clearCart()
                    loadOrders()
                    onResult(true, "Order #$orderId created successfully!")
                },
                onFailure = { error ->
                    onResult(false, error.message ?: "Order creation failed")
                }
            )
        }
    }
}
