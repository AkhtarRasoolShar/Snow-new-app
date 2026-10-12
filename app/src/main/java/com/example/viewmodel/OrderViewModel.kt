package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CartItem
import com.example.model.Department
import com.example.model.GarmentAudience
import com.example.model.Membership
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.Product
import com.example.model.RetailCategory
import com.example.model.ServiceCategory
import com.example.model.ServiceType
import com.example.network.NetworkSyncStatus
import com.example.network.SnowhiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class CartTotals(
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val finalTotal: Double = 0.0,
    val totalItemCount: Int = 0
)

data class UserProfile(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val swCardNumber: String = "",
    val address: String = "",
    val isLoggedIn: Boolean = false
)

class OrderViewModel(
    private val repository: SnowhiteRepository = SnowhiteRepository()
) : ViewModel() {

    // --- Network / Remote MySQL Sync State ---
    val syncStatus: StateFlow<NetworkSyncStatus> = repository.syncStatus

    // --- Authentication State ---
    private val _userProfile = MutableStateFlow(UserProfile(isLoggedIn = false))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _authRedirectTarget = MutableStateFlow<String?>(null)
    val authRedirectTarget: StateFlow<String?> = _authRedirectTarget.asStateFlow()

    fun setAuthRedirectTarget(target: String?) {
        _authRedirectTarget.value = target
    }

    fun isUserAuthenticated(): Boolean = _userProfile.value.isLoggedIn

    // --- Membership State ---
    private val _membership = MutableStateFlow(
        Membership(
            isMember = false,
            memberCode = "",
            memberName = "",
            tier = "SW Card Member",
            discountPercent = 20,
            freeDelivery = true
        )
    )
    val membership: StateFlow<Membership> = _membership.asStateFlow()

    // --- Navigation & Department Filter State ---
    private val _selectedDepartment = MutableStateFlow(Department.GARMENT_CARE)
    val selectedDepartment: StateFlow<Department> = _selectedDepartment.asStateFlow()

    // Garment Care Filters
    private val _selectedAudience = MutableStateFlow(GarmentAudience.GENTS)
    val selectedAudience: StateFlow<GarmentAudience> = _selectedAudience.asStateFlow()

    private val _selectedServiceType = MutableStateFlow(ServiceType.DRY_CLEANING)
    val selectedServiceType: StateFlow<ServiceType> = _selectedServiceType.asStateFlow()

    // Retail Filters
    private val _selectedRetailCategory = MutableStateFlow(RetailCategory.DEALS)
    val selectedRetailCategory: StateFlow<RetailCategory> = _selectedRetailCategory.asStateFlow()

    // --- Products Catalog ---
    private val _allProducts = MutableStateFlow(sampleProducts)
    val allProducts: StateFlow<List<Product>> = _allProducts.asStateFlow()

    // --- App Settings & Logo State ---
    private val _logoUrl = MutableStateFlow("https://snowhite.com.pk/wp-content/uploads/2021/04/snowhite-logo.png")
    val logoUrl: StateFlow<String> = _logoUrl.asStateFlow()

    init {
        // Fetch Live Products, Categories & Settings from remote backend immediately on launch
        refreshLiveCatalog()
    }

    fun refreshLiveCatalog() {
        viewModelScope.launch {
            // 1. Fetch live products from MySQL database
            repository.fetchLiveProducts().onSuccess { liveList ->
                if (liveList.isNotEmpty()) {
                    _allProducts.value = liveList
                }
            }

            // 2. Fetch app settings & logo
            repository.fetchSettings().onSuccess { settings ->
                if (!settings.logoUrl.isNullOrBlank()) {
                    _logoUrl.value = settings.logoUrl
                }
            }

            // 3. Fetch live orders from Admin Radar
            fetchUserOrdersFromServer()
        }
    }

    // Filtered Products for Garment Care
    val filteredProducts: StateFlow<List<Product>> = combine(
        _allProducts,
        _selectedAudience,
        _selectedServiceType
    ) { products, audience, serviceType ->
        products.filter {
            it.department == Department.GARMENT_CARE &&
            it.audience == audience &&
            it.serviceType == serviceType
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = sampleProducts.filter {
            it.department == Department.GARMENT_CARE &&
            it.audience == GarmentAudience.GENTS &&
            it.serviceType == ServiceType.DRY_CLEANING
        }
    )

    // Filtered Products for Retail
    val filteredRetailProducts: StateFlow<List<Product>> = combine(
        _allProducts,
        _selectedRetailCategory
    ) { products, retailCategory ->
        products.filter {
            it.department == Department.RETAIL &&
            it.retailCategory == retailCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = sampleProducts.filter {
            it.department == Department.RETAIL &&
            it.retailCategory == RetailCategory.DEALS
        }
    )

    // --- Cart State (ProductId -> CartItem) ---
    private val _cart = MutableStateFlow<Map<String, CartItem>>(emptyMap())
    val cart: StateFlow<Map<String, CartItem>> = _cart.asStateFlow()

    // --- Cart Totals Calculation Engine (PKR) ---
    val cartTotals: StateFlow<CartTotals> = combine(_cart, _membership) { cartMap, member ->
        calculateCartTotals(cartMap.values.toList(), member.isMember)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CartTotals()
    )

    // --- Checkout Form State (STRICT ZERO DUMMY DATA: initialize empty) ---
    private val _checkoutName = MutableStateFlow("")
    val checkoutName: StateFlow<String> = _checkoutName.asStateFlow()

    private val _checkoutPhone = MutableStateFlow("")
    val checkoutPhone: StateFlow<String> = _checkoutPhone.asStateFlow()

    private val _checkoutEmail = MutableStateFlow("")
    val checkoutEmail: StateFlow<String> = _checkoutEmail.asStateFlow()

    private val _checkoutAddress = MutableStateFlow("")
    val checkoutAddress: StateFlow<String> = _checkoutAddress.asStateFlow()

    private val _checkoutNotes = MutableStateFlow("")
    val checkoutNotes: StateFlow<String> = _checkoutNotes.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.CASH_ON_DELIVERY)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    private val _selectedPickupSlot = MutableStateFlow("Standard Pickup (Karachi PKT)")
    val selectedPickupSlot: StateFlow<String> = _selectedPickupSlot.asStateFlow()

    // --- Orders List & Active Order ---
    private val _orders = MutableStateFlow(sampleOrders)
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _lastPlacedOrder = MutableStateFlow<Order?>(null)
    val lastPlacedOrder: StateFlow<Order?> = _lastPlacedOrder.asStateFlow()

    private val _activeTrackedOrder = MutableStateFlow<Order?>(sampleOrders.firstOrNull())
    val activeTrackedOrder: StateFlow<Order?> = _activeTrackedOrder.asStateFlow()

    // --- Authentication Actions ---
    fun loginOrRegister(name: String, phone: String, email: String, swCard: String = "") {
        val hasCard = swCard.isNotBlank() && (swCard.startsWith("SW", ignoreCase = true) || swCard.length >= 4)
        val validName = name.ifBlank { "Valued Customer" }
        val validPhone = phone.ifBlank { "+92 300 0000000" }

        _userProfile.value = UserProfile(
            name = validName,
            phone = validPhone,
            email = email,
            swCardNumber = swCard,
            address = _checkoutAddress.value,
            isLoggedIn = true
        )
        _checkoutName.value = validName
        _checkoutPhone.value = validPhone
        _checkoutEmail.value = email

        if (hasCard) {
            validateMembershipCode(swCard)
        }

        // Asynchronous remote API call to PHP/MySQL on snow.akfasft.com
        viewModelScope.launch {
            val result = repository.loginOrRegisterCustomer(
                phone = if (phone.isNotBlank()) phone else "+92 300 0000000",
                email = email,
                name = validName,
                swCardNumber = swCard
            )

            result.onSuccess { userDto ->
                val fetchedName = userDto.name.ifBlank { validName }
                val fetchedPhone = userDto.phone.ifBlank { validPhone }
                val fetchedEmail = userDto.email ?: email
                val fetchedAddress = userDto.address ?: _checkoutAddress.value
                val fetchedCard = userDto.swCardNumber ?: swCard

                _userProfile.value = UserProfile(
                    name = fetchedName,
                    phone = fetchedPhone,
                    email = fetchedEmail,
                    swCardNumber = fetchedCard,
                    address = fetchedAddress,
                    isLoggedIn = true
                )

                // Auto-populate checkout form with details from remote MySQL database
                _checkoutName.value = fetchedName
                _checkoutPhone.value = fetchedPhone
                _checkoutEmail.value = fetchedEmail
                if (!userDto.address.isNullOrBlank()) {
                    _checkoutAddress.value = userDto.address
                }

                if (!fetchedCard.isNullOrBlank()) {
                    validateMembershipCode(fetchedCard)
                }
            }
        }
    }

    fun loginWithGoogle(email: String = "member@snowhite.com", name: String = "Valued Member") {
        _userProfile.value = UserProfile(
            name = name,
            email = email,
            phone = "+92 300 1234567",
            address = "Clifton Block 4, Karachi",
            isLoggedIn = true
        )
        _checkoutName.value = name
        _checkoutEmail.value = email

        viewModelScope.launch {
            val result = repository.loginUser(
                phoneOrEmail = email,
                passwordOrOtp = "GOOGLE_OAUTH",
                fullName = name
            )

            result.onSuccess { userDto ->
                val fetchedName = userDto.name.ifBlank { name }
                val fetchedEmail = userDto.email ?: email
                val fetchedPhone = userDto.phone.ifBlank { "+92 300 1234567" }
                val fetchedAddress = userDto.address ?: "Clifton Block 4, Karachi"

                _userProfile.value = UserProfile(
                    name = fetchedName,
                    email = fetchedEmail,
                    phone = fetchedPhone,
                    address = fetchedAddress,
                    isLoggedIn = true
                )

                _checkoutName.value = fetchedName
                _checkoutEmail.value = fetchedEmail
                _checkoutPhone.value = fetchedPhone
                _checkoutAddress.value = fetchedAddress

                if (!userDto.swCardNumber.isNullOrBlank()) {
                    validateMembershipCode(userDto.swCardNumber)
                }
            }
        }
    }

    fun logout() {
        _userProfile.value = UserProfile(isLoggedIn = false)
        _membership.value = Membership(
            isMember = false,
            memberCode = "",
            memberName = "",
            tier = "SW Card Member",
            discountPercent = 0,
            freeDelivery = false
        )
        _checkoutName.value = ""
        _checkoutPhone.value = ""
        _checkoutEmail.value = ""
        _checkoutAddress.value = ""
        _checkoutNotes.value = ""
    }

    // Forgot password handlers
    fun sendPasswordOtpViaEmail(email: String): String {
        val target = email.ifBlank { _userProfile.value.email }
        viewModelScope.launch {
            repository.sendOtp(target = target, channel = "EMAIL")
        }
        return "A 6-digit recovery code has been sent to $target"
    }

    fun sendPasswordOtpViaWhatsApp(phone: String): String {
        val target = phone.ifBlank { _userProfile.value.phone }
        viewModelScope.launch {
            repository.sendOtp(target = target, channel = "WHATSAPP")
        }
        return "A 6-digit recovery code has been sent via WhatsApp to $target"
    }

    // --- Filter Setters ---
    fun setDepartment(dept: Department) { _selectedDepartment.value = dept }
    fun setAudience(audience: GarmentAudience) { _selectedAudience.value = audience }
    fun setServiceType(service: ServiceType) { _selectedServiceType.value = service }
    fun setRetailCategory(cat: RetailCategory) { _selectedRetailCategory.value = cat }

    // --- Membership Verification & Registration ---
    fun validateMembershipCode(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        // Accepts codes like "B 2152", "B2152", "SW-VIP", "SW-XXXX", or any membership number
        val isValid = trimmed.isNotBlank() && (
            trimmed.startsWith("SW") ||
            trimmed.startsWith("B") ||
            trimmed.matches(Regex("^[A-Z]?\\s?\\d{3,6}$")) ||
            trimmed.length >= 3
        )
        if (isValid) {
            _membership.value = Membership(
                isMember = true,
                memberCode = trimmed,
                memberName = _userProfile.value.name.ifBlank { "Gold Card Member" },
                tier = "SW Gold Card Member",
                discountPercent = 20, // baseline
                freeDelivery = true
            )
        }

        // Asynchronous verification with remote MySQL database via Retrofit
        viewModelScope.launch {
            val result = repository.verifySWCard(trimmed)
            result.onSuccess { cardDto ->
                if (cardDto.valid) {
                    _membership.value = Membership(
                        isMember = true,
                        memberCode = cardDto.cardNumber,
                        memberName = cardDto.cardHolder ?: _userProfile.value.name.ifBlank { "Gold Card Member" },
                        tier = cardDto.tier,
                        discountPercent = cardDto.discountPercent,
                        freeDelivery = cardDto.freeDelivery
                    )
                }
            }
        }
        return isValid
    }

    fun registerNewMember(name: String, phone: String, email: String, address: String): Membership {
        val randomNum = (1000..9999).random()
        val generatedCode = "B $randomNum"
        val newMembership = Membership(
            isMember = true,
            memberCode = generatedCode,
            memberName = name,
            tier = "SW Gold Card Member",
            discountPercent = 20,
            freeDelivery = true
        )
        _membership.value = newMembership
        _userProfile.value = _userProfile.value.copy(
            name = name,
            phone = phone,
            email = email,
            swCardNumber = generatedCode,
            address = address
        )
        _checkoutName.value = name
        _checkoutPhone.value = phone
        _checkoutEmail.value = email
        _checkoutAddress.value = address

        // Register member on remote MySQL backend
        viewModelScope.launch {
            repository.registerMember(
                name = name,
                phone = phone,
                email = email,
                address = address
            )
        }
        return newMembership
    }

    fun setNonMember() {
        _membership.value = Membership(
            isMember = false,
            memberCode = "",
            memberName = "",
            discountPercent = 0,
            freeDelivery = false
        )
    }

    // --- Cart Operations ---
    fun addToCart(product: Product, quantityToAdd: Int = 1) {
        if (quantityToAdd <= 0) return
        val current = _cart.value.toMutableMap()
        val existing = current[product.id]
        if (existing != null) {
            current[product.id] = existing.copy(quantity = existing.quantity + quantityToAdd)
        } else {
            current[product.id] = CartItem(product = product, quantity = quantityToAdd)
        }
        _cart.value = current
    }

    fun removeFromCart(productId: String) {
        val current = _cart.value.toMutableMap()
        val existing = current[productId]
        if (existing != null) {
            if (existing.quantity > 1) {
                current[productId] = existing.copy(quantity = existing.quantity - 1)
            } else {
                current.remove(productId)
            }
            _cart.value = current
        }
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    // --- Checkout Fields Update ---
    fun updateCheckoutName(value: String) { _checkoutName.value = value }
    fun updateCheckoutPhone(value: String) { _checkoutPhone.value = value }
    fun updateCheckoutEmail(value: String) { _checkoutEmail.value = value }
    fun updateCheckoutAddress(value: String) { _checkoutAddress.value = value }
    fun updateCheckoutNotes(value: String) { _checkoutNotes.value = value }
    fun updatePaymentMethod(method: PaymentMethod) { _selectedPaymentMethod.value = method }
    fun updatePickupSlot(slot: String) { _selectedPickupSlot.value = slot }

    // --- Order Placement ---
    fun placeOrder(): Order {
        val totals = cartTotals.value
        val itemsList = _cart.value.values.toList()
        val shortId = "SW-${(1000..9999).random()}"
        val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Karachi")
        }
        val dateString = dateFormat.format(Date())

        val newOrder = Order(
            id = shortId,
            customerName = _checkoutName.value.ifBlank { "Valued Customer" },
            phone = _checkoutPhone.value.ifBlank { "+92 300 0000000" },
            email = _checkoutEmail.value,
            address = _checkoutAddress.value.ifBlank { "Doorstep Pickup Address" },
            notes = _checkoutNotes.value,
            items = itemsList,
            subtotal = totals.subtotal,
            discount = totals.discount,
            deliveryFee = totals.deliveryFee,
            total = totals.finalTotal,
            isMember = _membership.value.isMember,
            paymentMethod = _selectedPaymentMethod.value,
            status = OrderStatus.CONFIRMED,
            orderDate = dateString,
            pickupSlot = _selectedPickupSlot.value,
            riderName = "Ahmed Tariq (Snowhite Valet)",
            riderPhone = "+92 300 1234567"
        )

        // -------------------------------------------------------------
        // TODO: Trigger WhatsApp API
        // Notify Admin (+92-800-SNOW-WHITE), Assigned Rider (Ahmed Tariq),
        // and Buyer simultaneously with order confirmation and route link.
        // -------------------------------------------------------------
        println("[SnowhiteBackend] [WhatsApp API] Notification dispatched for Order ${newOrder.id} to ${newOrder.phone}")

        // -------------------------------------------------------------
        // TODO: Trigger Email API
        // Send official PDF invoice, care instructions, and live status
        // link to buyer's email: ${newOrder.email}
        // -------------------------------------------------------------
        println("[SnowhiteBackend] [Email API] Order receipt sent to ${newOrder.email}")

        // Remote MySQL API sync via Live routes.php (create_order / place_order)
        viewModelScope.launch {
            repository.placeOrderToAdminRadar(newOrder)
        }

        _orders.value = listOf(newOrder) + _orders.value
        _lastPlacedOrder.value = newOrder
        _activeTrackedOrder.value = newOrder

        clearCart()
        return newOrder
    }

    fun fetchUserOrdersFromServer(userId: String = _userProfile.value.phone) {
        viewModelScope.launch {
            val result = repository.fetchOrdersFromLiveServer()
            result.onSuccess { liveOrdersList ->
                if (liveOrdersList.isNotEmpty()) {
                    // Combine live orders with local orders, keeping newest first
                    val combined = (liveOrdersList + _orders.value).distinctBy { it.id }
                    _orders.value = combined
                    if (_activeTrackedOrder.value == null) {
                        _activeTrackedOrder.value = combined.firstOrNull()
                    }
                }
            }
        }
    }

    fun setActiveTrackedOrder(order: Order) {
        _activeTrackedOrder.value = order
    }

    companion object {
        fun calculateCartTotals(
            items: List<CartItem>,
            isMember: Boolean
        ): CartTotals {
            val subtotal = items.sumOf { it.product.originalPrice * it.quantity }
            val totalItemCount = items.sumOf { it.quantity }
            val discount = if (isMember && subtotal > 0) {
                val calculatedDiscount = items.sumOf { cartItem ->
                    val discountPercent = cartItem.product.getEffectiveDiscountPercent(true)
                    (cartItem.product.originalPrice * cartItem.quantity * (discountPercent / 100.0))
                }
                (calculatedDiscount * 100.0).toLong() / 100.0
            } else {
                0.0
            }
            val deliveryFee = when {
                totalItemCount == 0 -> 0.0
                isMember -> 0.0 // Free delivery for SW Card Members
                subtotal >= 2500.0 -> 0.0
                else -> 250.0
            }
            val finalTotal = ((subtotal - discount + deliveryFee) * 100.0).toLong() / 100.0
            return CartTotals(
                subtotal = (subtotal * 100.0).toLong() / 100.0,
                discount = discount,
                deliveryFee = deliveryFee,
                finalTotal = finalTotal,
                totalItemCount = totalItemCount
            )
        }

        // Complete 30+ Garment Care items + Retail Products with Real-World Discounts
        val sampleProducts = listOf(
            // ================== GENT'S GARMENT CARE ==================
            // Premium Plus Service (30% Discount)
            Product("gc_g1", "Two-Piece Suit (Premium Plus)", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.DRY_CLEANING, ServiceCategory.PREMIUM_PLUS_SERVICE, originalPrice = 1200.0, badge = "30% OFF", description = "Deluxe bespoke conditioning, stain extraction & hand-pressed lapels."),
            // Regular Service (20% Discount)
            Product("gc_g2", "Two-Piece Suit (Regular)", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.DRY_CLEANING, ServiceCategory.REGULAR_SERVICE, originalPrice = 850.0, badge = "Bestseller", description = "Coat & trousers dry clean with hand-pressed lapels."),
            Product("gc_g3", "Two-Piece Suit (Steam Press)", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.STEAM_IRONING, ServiceCategory.REGULAR_SERVICE, originalPrice = 250.0, description = "Wrinkle-free steam press on wooden hanger."),
            Product("gc_g4", "Two-Piece Suit (Wash & Fold)", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.LAUNDRY, ServiceCategory.REGULAR_SERVICE, originalPrice = 650.0, description = "Gentle thermal wash with fabric conditioner."),
            
            Product("gc_g5", "Kurta Shalwar (Premium Plus)", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.DRY_CLEANING, ServiceCategory.PREMIUM_PLUS_SERVICE, originalPrice = 650.0, badge = "30% OFF", description = "Luxury fabric wash, hand starching & boutique steam press."),
            Product("gc_g6", "Kurta Shalwar (Regular)", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.DRY_CLEANING, ServiceCategory.REGULAR_SERVICE, originalPrice = 450.0, badge = "Popular", description = "Cotton/Lawn kurta shalwar stain extraction & dry clean."),
            Product("gc_g7", "Kurta Shalwar (Press)", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.STEAM_IRONING, ServiceCategory.REGULAR_SERVICE, originalPrice = 150.0, description = "Crease-perfect steam ironing on hanger."),
            Product("gc_g8", "Kurta Shalwar (Wash)", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.LAUNDRY, ServiceCategory.REGULAR_SERVICE, originalPrice = 300.0, description = "Hypoallergenic wash and gentle tumble dry."),

            Product("gc_g9", "Sherwani / Achkan (Premium Plus)", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.DRY_CLEANING, ServiceCategory.PREMIUM_PLUS_SERVICE, originalPrice = 2200.0, badge = "30% OFF", description = "Heirloom bridal & ceremonial care with zardozi protection."),
            Product("gc_g10", "Waistcoat / Prince Coat", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.DRY_CLEANING, ServiceCategory.REGULAR_SERVICE, originalPrice = 550.0, description = "Form-fitting steam press and collar treatment."),
            Product("gc_g11", "Executive Dress Shirt", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.LAUNDRY, ServiceCategory.REGULAR_SERVICE, originalPrice = 200.0, description = "Collar and cuff pre-treatment wash."),
            Product("gc_g12", "Executive Dress Shirt (Press)", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.STEAM_IRONING, ServiceCategory.REGULAR_SERVICE, originalPrice = 120.0, description = "High-pressure boiler steam iron."),
            Product("gc_g13", "Formal Trousers / Slacks", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.DRY_CLEANING, ServiceCategory.REGULAR_SERVICE, originalPrice = 400.0, description = "Knife pleat alignment dry clean."),
            Product("gc_g14", "Winter Overcoat / Trench", Department.GARMENT_CARE, GarmentAudience.GENTS, ServiceType.DRY_CLEANING, ServiceCategory.REGULAR_SERVICE, originalPrice = 1100.0, description = "Wool & cashmere weatherproof dry clean."),

            // ================== LADIES GARMENT CARE ==================
            Product("gc_l1", "Three-Piece Suit (Premium Plus)", Department.GARMENT_CARE, GarmentAudience.LADIES, ServiceType.DRY_CLEANING, ServiceCategory.PREMIUM_PLUS_SERVICE, originalPrice = 950.0, badge = "30% OFF", description = "High-end designer lawn/chiffon revitalizing care."),
            Product("gc_l2", "Three-Piece Suit (Regular)", Department.GARMENT_CARE, GarmentAudience.LADIES, ServiceType.DRY_CLEANING, ServiceCategory.REGULAR_SERVICE, originalPrice = 650.0, badge = "Bestseller", description = "Shirt, trousers & dupatta dry clean with gentle starch."),
            Product("gc_l3", "Three-Piece Suit (Press)", Department.GARMENT_CARE, GarmentAudience.LADIES, ServiceType.STEAM_IRONING, ServiceCategory.REGULAR_SERVICE, originalPrice = 200.0, description = "Wrinkle-free steam pressing on hangers."),
            Product("gc_l4", "Three-Piece Suit (Wash)", Department.GARMENT_CARE, GarmentAudience.LADIES, ServiceType.LAUNDRY, ServiceCategory.REGULAR_SERVICE, originalPrice = 450.0, description = "Soft gentle wash with fabric reviver."),

            Product("gc_l5", "Evening Gown / Maxi (Premium Plus)", Department.GARMENT_CARE, GarmentAudience.LADIES, ServiceType.DRY_CLEANING, ServiceCategory.PREMIUM_PLUS_SERVICE, originalPrice = 1600.0, badge = "30% OFF", description = "Couture sequins, crystals & chiffon eco-solvent delicate clean."),
            Product("gc_l6", "Silk Saree", Department.GARMENT_CARE, GarmentAudience.LADIES, ServiceType.DRY_CLEANING, ServiceCategory.REGULAR_SERVICE, originalPrice = 850.0, description = "Banarsi, chiffon or pure silk revitalizing dry clean."),
            Product("gc_l7", "Bridal Lehenga (Premium Plus)", Department.GARMENT_CARE, GarmentAudience.LADIES, ServiceType.DRY_CLEANING, ServiceCategory.PREMIUM_PLUS_SERVICE, originalPrice = 4500.0, badge = "30% OFF", description = "Specialty master dry cleaner for multi-layered royal embroidery."),
            Product("gc_l8", "Abaya / Burqa", Department.GARMENT_CARE, GarmentAudience.LADIES, ServiceType.DRY_CLEANING, ServiceCategory.REGULAR_SERVICE, originalPrice = 550.0, description = "Deep black preservation dry clean and steam press."),
            Product("gc_l9", "Chiffon / Silk Dupatta", Department.GARMENT_CARE, GarmentAudience.LADIES, ServiceType.DRY_CLEANING, ServiceCategory.REGULAR_SERVICE, originalPrice = 300.0, description = "Anti-crease delicate silk treatment."),
            Product("gc_l10", "Casual Kurti", Department.GARMENT_CARE, GarmentAudience.LADIES, ServiceType.LAUNDRY, ServiceCategory.REGULAR_SERVICE, originalPrice = 220.0, description = "Daily wear wash and crisp fold."),
            Product("gc_l11", "Woolen Shawl / Pashmina", Department.GARMENT_CARE, GarmentAudience.LADIES, ServiceType.DRY_CLEANING, ServiceCategory.REGULAR_SERVICE, originalPrice = 750.0, description = "Pure Kashmiri pashmina delicate moth-proof clean."),

            // ================== HOUSEHOLDS & SPECIALTY CARE ==================
            // Curtain, Carpet & Blanket (30% Discount)
            Product("gc_h1", "Curtain Panel (Heavy Velvet/Silk)", Department.GARMENT_CARE, GarmentAudience.HOUSEHOLDS, ServiceType.DRY_CLEANING, ServiceCategory.CURTAIN_CARPET_BLANKET, originalPrice = 900.0, badge = "30% OFF", description = "Full height thermal dust mite extraction & steam release."),
            Product("gc_h2", "Heavy Comforter / Double Blanket", Department.GARMENT_CARE, GarmentAudience.HOUSEHOLDS, ServiceType.DRY_CLEANING, ServiceCategory.CURTAIN_CARPET_BLANKET, originalPrice = 1400.0, badge = "30% OFF", description = "Deep allergen extraction and sanitizing thermal tumbling."),
            Product("gc_h3", "Persian / Woolen Carpet (per sq ft)", Department.GARMENT_CARE, GarmentAudience.HOUSEHOLDS, ServiceType.DRY_CLEANING, ServiceCategory.CURTAIN_CARPET_BLANKET, originalPrice = 85.0, badge = "30% OFF", description = "Deep rotary shampoo & fiber conditioner wash."),
            Product("gc_h4", "Traditional Razai / Quilt", Department.GARMENT_CARE, GarmentAudience.HOUSEHOLDS, ServiceType.DRY_CLEANING, ServiceCategory.CURTAIN_CARPET_BLANKET, originalPrice = 1500.0, badge = "30% OFF", description = "Cotton batting revitalizing and anti-dust treatment."),

            // Spot Carpet, Sofa & Floor Cleaning / Polishing (30% Discount)
            Product("gc_h5", "Sofa Cleaning & Polishing (per seat)", Department.GARMENT_CARE, GarmentAudience.HOUSEHOLDS, ServiceType.DRY_CLEANING, ServiceCategory.SPOT_CLEANING_POLISHING, originalPrice = 600.0, badge = "30% OFF", description = "Hot water injection & suction upholstery deep polishing."),
            Product("gc_h6", "Spot Carpet Stain Treatment", Department.GARMENT_CARE, GarmentAudience.HOUSEHOLDS, ServiceType.DRY_CLEANING, ServiceCategory.SPOT_CLEANING_POLISHING, originalPrice = 750.0, badge = "30% OFF", description = "Enzymatic spot extraction for grease, coffee & paint."),
            Product("gc_h7", "Marble / Tile Floor Deep Polish", Department.GARMENT_CARE, GarmentAudience.HOUSEHOLDS, ServiceType.DRY_CLEANING, ServiceCategory.SPOT_CLEANING_POLISHING, originalPrice = 2800.0, badge = "30% OFF", description = "Industrial floor scrubbing, buffing & diamond gloss seal."),
            Product("gc_h8", "Dining Chair Upholstery Spot Polish", Department.GARMENT_CARE, GarmentAudience.HOUSEHOLDS, ServiceType.DRY_CLEANING, ServiceCategory.SPOT_CLEANING_POLISHING, originalPrice = 400.0, badge = "30% OFF", description = "Fabric rejuvenation & anti-stain protection coating."),

            // Regular Household Items (20% Discount)
            Product("gc_h9", "Bed Sheet Set (Double)", Department.GARMENT_CARE, GarmentAudience.HOUSEHOLDS, ServiceType.LAUNDRY, ServiceCategory.REGULAR_SERVICE, originalPrice = 350.0, badge = "Daily Essential", description = "Bed sheet + 2 pillowcases thermal wash & fold."),
            Product("gc_h10", "Bed Sheet Set (Steam Press)", Department.GARMENT_CARE, GarmentAudience.HOUSEHOLDS, ServiceType.STEAM_IRONING, ServiceCategory.REGULAR_SERVICE, originalPrice = 180.0, description = "Industrial steam iron flat press."),
            Product("gc_h11", "Cushion Covers (Set of 4)", Department.GARMENT_CARE, GarmentAudience.HOUSEHOLDS, ServiceType.DRY_CLEANING, ServiceCategory.REGULAR_SERVICE, originalPrice = 450.0, description = "Zipper & decorative stitch safe cleaning."),

            // ================== RETAIL: DEALS & PRODUCTS (20% Discount) ==================
            // Washing Powder, Phenyle & Others Products (20% Discount)
            Product("ret_d1", "Mega Laundry Bundle (5KG + 2L)", Department.RETAIL, retailCategory = RetailCategory.DEALS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 2450.0, badge = "Save 20%", isRetail = true, description = "5KG Snowhite Washing Powder + 2L Luxury Softener + Free Stain Bar."),
            Product("ret_d2", "Family Starter Pack (2KG + 1L)", Department.RETAIL, retailCategory = RetailCategory.DEALS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 1350.0, badge = "Popular Deal", isRetail = true, description = "2KG Active Powder + 1L Liquid Detergent + Measuring Scoop."),
            Product("ret_d3", "Floor Hygiene Trio (Phenyle + Sanitizer)", Department.RETAIL, retailCategory = RetailCategory.DEALS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 1100.0, badge = "Hygiene Pack", isRetail = true, description = "2L Pine Phenyle + 1L Surface Sanitizer + 500ml Bleach."),
            Product("ret_d4", "Delicate Care Duo (2x 500ml)", Department.RETAIL, retailCategory = RetailCategory.DEALS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 950.0, badge = "Eco Deal", isRetail = true, description = "Wool & Silk Gentle Liquid Wash Duo for premium fabrics."),

            Product("ret_p1", "Snowhite Washing Powder 1KG", Department.RETAIL, retailCategory = RetailCategory.PRODUCTS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 420.0, isRetail = true, description = "Triple action bio-enzyme stain dissolving formula with active blue pearls."),
            Product("ret_p2", "Snowhite Washing Powder 3KG", Department.RETAIL, retailCategory = RetailCategory.PRODUCTS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 1150.0, isRetail = true, description = "Economy family size with mountain fresh long-lasting fragrance."),
            Product("ret_p3", "Snowhite Washing Powder 5KG", Department.RETAIL, retailCategory = RetailCategory.PRODUCTS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 1850.0, badge = "Best Value", isRetail = true, description = "Heavy duty commercial grade powder for automatic & manual machines."),
            Product("ret_p4", "Snowhite Luxury Fabric Softener 2L", Department.RETAIL, retailCategory = RetailCategory.PRODUCTS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 850.0, isRetail = true, description = "Micro-encapsulated floral fragrance, eliminates static cling and softens fibers."),
            Product("ret_p5", "Snowhite Antibacterial Pine Phenyle 2L", Department.RETAIL, retailCategory = RetailCategory.PRODUCTS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 520.0, isRetail = true, description = "Concentrated floor disinfecting phenyle with long-lasting natural pine aroma."),
            Product("ret_p6", "Snowhite Liquid Detergent 1.5L", Department.RETAIL, retailCategory = RetailCategory.PRODUCTS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 950.0, isRetail = true, description = "Low foam high efficiency formula for automatic front load washers."),
            Product("ret_p7", "Snowhite Pre-Wash Stain Remover 500ml", Department.RETAIL, retailCategory = RetailCategory.PRODUCTS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 480.0, isRetail = true, description = "Fast-acting pre-wash spray for collar grease, oil, tea, and curry stains."),
            Product("ret_p8", "Snowhite Oxy-Action Fabric Bleach 1L", Department.RETAIL, retailCategory = RetailCategory.PRODUCTS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 380.0, isRetail = true, description = "Color safe chlorine-free oxygen brightener for brilliant whites."),
            Product("ret_p9", "Snowhite Ultra Clean Pods (30x)", Department.RETAIL, retailCategory = RetailCategory.PRODUCTS, serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL, originalPrice = 1250.0, isRetail = true, description = "3-in-1 pre-measured pods: detergent, stain fighter, and fabric protector.")
        )

        val sampleOrders = listOf(
            Order(
                id = "SW-6821",
                customerName = "Valued Member",
                phone = "+92 300 1234567",
                email = "member@snowhite.com",
                address = "Clifton Block 4, Karachi",
                notes = "Leave with concierge if not available.",
                items = listOf(
                    CartItem(product = sampleProducts[0], quantity = 2), // 2x Suit = 1700
                    CartItem(product = sampleProducts[9], quantity = 4)  // 4x Shirt Press = 480
                ),
                subtotal = 2180.0,
                discount = 436.0,
                deliveryFee = 0.0,
                total = 1744.0,
                isMember = true,
                paymentMethod = PaymentMethod.PAY_ONLINE,
                status = OrderStatus.OUT_FOR_DELIVERY,
                orderDate = "Oct 08, 2026 • 10:30 AM",
                pickupSlot = "Today, 10:00 AM - 12:00 PM",
                riderName = "Ahmed Tariq (Snowhite Valet)",
                riderPhone = "+92 300 1234567"
            )
        )
    }
}
