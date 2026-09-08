package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.UserRole
import com.example.ui.auth.UserSession
import com.example.ui.consumer.ConsumerScreen
import com.example.ui.courier.CourierScreen
import com.example.ui.merchant.MerchantScreen
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val repository = AppRepository()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          AppRootNavigation(repository = repository)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRootNavigation(repository: AppRepository) {
  // Current logged in user session (Null indicates user is on the Registration / Role selection screen)
  var currentSession by remember { mutableStateOf<UserSession?>(null) }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val stores by repository.stores.collectAsStateWithLifecycle()
  val products by repository.products.collectAsStateWithLifecycle()
  val reviews by repository.reviews.collectAsStateWithLifecycle()
  val deliveryOrders by repository.deliveryOrders.collectAsStateWithLifecycle()
  val cart by repository.cart.collectAsStateWithLifecycle()
  val courier by repository.courier.collectAsStateWithLifecycle()
  val currentOwnerStoreId by repository.currentOwnerStoreId.collectAsStateWithLifecycle()
  val activeCourierAlert by repository.activeCourierAlertOrder.collectAsStateWithLifecycle()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      if (currentSession != null) {
        val session = currentSession!!
        TopAppBar(
          title = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = when (session.role) {
                  UserRole.MERCHANT -> AmberPrimary
                  UserRole.CONSUMER -> MintGreen
                  UserRole.COURIER -> DeliveryBlue
                },
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(session.role.emoji, fontSize = 18.sp)
                }
              }
              Column {
                Text(
                  text = session.name,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1E293B)
                )
                Text(
                  text = when (session.role) {
                    UserRole.MERCHANT -> "واجهة التاجر (${session.storeName ?: "المتجر"})"
                    UserRole.CONSUMER -> "واجهة المستهلك والتسوق"
                    UserRole.COURIER -> "واجهة موصل الطلبات"
                  },
                  fontSize = 11.sp,
                  color = Color(0xFF64748B)
                )
              }
            }
          },
          actions = {
            // Button to switch account / logout and return to registration
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFF1F5F9),
              modifier = Modifier
                .padding(end = 8.dp)
                .clickable {
                  currentSession = null
                  scope.launch {
                    snackbarHostState.showSnackbar("تم تسجيل الخروج، اختر نوع الحساب مجدداً")
                  }
                }
                .testTag("btn_switch_role_logout")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.SwapHoriz,
                  contentDescription = "تبديل الحساب",
                  tint = Color(0xFF475569),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "تبديل الحساب",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF475569)
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      AnimatedContent(
        targetState = currentSession,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "auth_screen_transition"
      ) { session ->
        if (session == null) {
          // Dedicated Registration & Login Screen where user selects: Merchant, Consumer, or Courier
          AuthScreen(
            repository = repository,
            onLoginSuccess = { loggedInSession ->
              currentSession = loggedInSession
              scope.launch {
                snackbarHostState.showSnackbar("مرحباً بك كـ ${loggedInSession.role.titleArabic}!")
              }
            }
          )
        } else {
          // Dedicated Separated Interface based on the registration role
          when (session.role) {
            UserRole.MERCHANT -> {
              MerchantScreen(
                repository = repository,
                stores = stores,
                products = products,
                reviews = reviews,
                currentStoreId = currentOwnerStoreId,
                onShowSnackbar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
              )
            }
            UserRole.CONSUMER -> {
              ConsumerScreen(
                repository = repository,
                stores = stores,
                products = products,
                cart = cart,
                onShowSnackbar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
              )
            }
            UserRole.COURIER -> {
              CourierScreen(
                repository = repository,
                stores = stores,
                deliveryOrders = deliveryOrders,
                courierProfile = courier,
                activeAlertOrder = activeCourierAlert,
                onShowSnackbar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
              )
            }
          }
        }
      }
    }
  }
}
