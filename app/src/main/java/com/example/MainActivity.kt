package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.ui.screens.*
import com.example.ui.theme.SnowhiteTheme
import com.example.viewmodel.SnowhiteViewModel
import kotlinx.coroutines.launch

fun NavHostController.safePopBackStack() {
    if (this.previousBackStackEntry != null) {
        this.popBackStack()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnowhiteTheme {
                SnowhiteMainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnowhiteMainApp() {
    val context = LocalContext.current
    val viewModel: SnowhiteViewModel = viewModel()
    val navController = rememberNavController()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val cartItems by viewModel.cartItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSwMember by viewModel.isSwMember.collectAsState()

    val totalCartQty = remember(cartItems) { cartItems.sumOf { it.quantity } }
    val grandTotal = viewModel.calculateGrandTotal()

    if (currentRoute == "home") {
        BackHandler(enabled = drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute == "home" || drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0061E0))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Snowhite",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Laundry & Dry Cleaning • Karachi",
                            fontSize = 12.sp,
                            color = Color(0xFFE0EFFE)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (currentUser != null) {
                            Text(
                                text = "Logged in as:",
                                fontSize = 11.sp,
                                color = Color(0xFFBAE6FD)
                            )
                            Text(
                                text = currentUser?.name ?: "Customer",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (isSwMember) {
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "SW MEMBER ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate("auth")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Log In / Register", color = Color(0xFF0061E0), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF0061E0)) },
                    label = { Text("Home", fontWeight = FontWeight.SemiBold) },
                    selected = currentRoute == "home",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("home") { launchSingleTop = true }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF0061E0)) },
                    label = { Text("Departments", fontWeight = FontWeight.SemiBold) },
                    selected = currentRoute == "departments",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("departments") { launchSingleTop = true }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF0061E0)) },
                    label = { Text("My Orders & Tracking", fontWeight = FontWeight.SemiBold) },
                    selected = currentRoute == "orders",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("orders") { launchSingleTop = true }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "HELP & SUPPORT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF16A34A)) },
                    label = { Text("WhatsApp Support (03111110500)", fontSize = 13.sp) },
                    selected = false,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/923111110500"))
                        context.startActivity(intent)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0061E0)) },
                    label = { Text("cs@snowhite.com.pk", fontSize = 13.sp) },
                    selected = false,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:cs@snowhite.com.pk"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Snowhite",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0061E0)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = Color(0xFF0061E0))
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("checkout") }) {
                            BadgedBox(
                                badge = {
                                    if (totalCartQty > 0) {
                                        Badge(containerColor = Color(0xFF0061E0), contentColor = Color.White) {
                                            Text("$totalCartQty")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color(0xFF0061E0))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        onClick = {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0061E0),
                            selectedTextColor = Color(0xFF0061E0),
                            indicatorColor = Color(0xFFF0F7FF)
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Category, contentDescription = "Departments") },
                        label = { Text("Departments") },
                        selected = currentRoute == "departments",
                        onClick = {
                            if (currentRoute != "departments") {
                                navController.navigate("departments") {
                                    launchSingleTop = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0061E0),
                            selectedTextColor = Color(0xFF0061E0),
                            indicatorColor = Color(0xFFF0F7FF)
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Tracking") },
                        label = { Text("Tracking") },
                        selected = currentRoute == "orders",
                        onClick = {
                            if (currentRoute != "orders") {
                                navController.navigate("orders") {
                                    launchSingleTop = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0061E0),
                            selectedTextColor = Color(0xFF0061E0),
                            indicatorColor = Color(0xFFF0F7FF)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToMembership = { navController.navigate("membership") },
                            onNavigateToCart = { navController.navigate("checkout") }
                        )
                    }
                    composable("membership") {
                        MembershipScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.safePopBackStack() },
                            onNavigateToDepartments = { navController.navigate("departments") },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }
                    composable("register") {
                        AuthScreen(
                            viewModel = viewModel,
                            initialRegisterMode = true,
                            onAuthSuccess = { navController.safePopBackStack() }
                        )
                    }

                    composable("departments") {
                        DepartmentsScreen(
                            onSelectLaundry = { navController.navigate("products/laundry") },
                            onSelectRetail = { navController.navigate("products/retail") }
                        )
                    }

                    composable("products/{department}") { backStackEntry ->
                        val dept = backStackEntry.arguments?.getString("department") ?: "all"
                        ProductListScreen(
                            departmentType = dept,
                            viewModel = viewModel,
                            onNavigateToCart = { navController.navigate("checkout") }
                        )
                    }

                    composable("checkout") {
                        CheckoutScreen(
                            viewModel = viewModel,
                            onNavigateToAuth = { navController.navigate("auth") },
                            onOrderPlacedSuccess = {
                                navController.navigate("orders") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        )
                    }

                    composable("orders") {
                        OrdersTrackingScreen(
                            viewModel = viewModel,
                            onNavigateToAuth = { navController.navigate("auth") }
                        )
                    }

                    composable("auth") {
                        AuthScreen(
                            viewModel = viewModel,
                            onAuthSuccess = { navController.safePopBackStack() }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = totalCartQty > 0 && currentRoute != "checkout",
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable {
                                if (currentUser == null) {
                                    navController.navigate("auth")
                                } else {
                                    navController.navigate("checkout")
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF0061E0),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "$totalCartQty",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Rs. ${grandTotal.toInt()} PKR",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "View Bag & Checkout",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Text(
                                text = "CONTINUE ->",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }
        }
    }
}
