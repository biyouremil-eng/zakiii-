package com.example.ui.courier

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRepository
import com.example.data.CourierProfile
import com.example.data.DeliveryOrder
import com.example.data.OrderStatus
import com.example.data.Store
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.MintGreen
import com.example.ui.theme.SuccessGreen

@Composable
fun CourierScreen(
  repository: AppRepository,
  stores: List<Store>,
  deliveryOrders: List<DeliveryOrder>,
  courierProfile: CourierProfile,
  activeAlertOrder: DeliveryOrder?,
  onShowSnackbar: (String) -> Unit
) {
  val context = LocalContext.current
  var isOnline by remember { mutableStateOf(courierProfile.isOnline) }
  var selectedOrderForRoute by remember {
    mutableStateOf(
      deliveryOrders.find { it.status == OrderStatus.ACCEPTED_BY_COURIER || it.status == OrderStatus.PICKED_UP_FROM_STORE }
        ?: activeAlertOrder
        ?: deliveryOrders.firstOrNull()
    )
  }

  // Active in-progress order for courier
  val currentActiveDelivery = deliveryOrders.find {
    it.status == OrderStatus.ACCEPTED_BY_COURIER || it.status == OrderStatus.PICKED_UP_FROM_STORE
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF1F5F9))
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // 1. Top Courier Status Bar
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
              modifier = Modifier.size(42.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.DirectionsBike,
                  contentDescription = null,
                  tint = if (isOnline) SuccessGreen else Color(0xFFD32F2F),
                  modifier = Modifier.size(22.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(courierProfile.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(
                text = if (isOnline) "🟢 متصل وجاهز لاستقبال الطلبات" else "🔴 غير متصل حالياً",
                fontSize = 11.sp,
                color = if (isOnline) SuccessGreen else Color(0xFFD32F2F),
                fontWeight = FontWeight.Medium
              )
            }
          }

          // Online Switch
          Switch(
            checked = isOnline,
            onCheckedChange = {
              isOnline = it
              onShowSnackbar(if (it) "أنت الآن متصل وتستقبل طلبات التوصيل" else "تم إيقاف استقبال الطلبات")
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = SuccessGreen
            )
          )
        }
      }

      // 2. Earnings and Trips Mini Cards
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
          Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = AmberDark, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("أرباح التوصيل", fontSize = 10.sp, color = Color(0xFF64748B))
              Text("${"%.1f".format(courierProfile.totalEarnings)} ر.س", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AmberDark)
            }
          }
        }

        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
          Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DoneAll, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("رحلات مكتملة", fontSize = 10.sp, color = Color(0xFF64748B))
              Text("${courierProfile.completedDeliveries} توصيلة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. The Interactive Map with Store Locations & Courier (Requirement: "اول مايفتح يجد خريطة معها مواقع محلات")
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 14.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
      ) {
        InteractiveMapCanvas(
          modifier = Modifier.fillMaxSize(),
          stores = stores,
          activeOrder = currentActiveDelivery ?: selectedOrderForRoute,
          courierLocation = courierProfile.location,
          userLocation = courierProfile.location
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 4. Delivery Orders / Active Task Bar at the bottom
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "طلبات التوصيل المسجلة في منطقتك (${deliveryOrders.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
          )

          Spacer(modifier = Modifier.height(6.dp))

          // If active in-progress delivery exists, display active progress actions
          if (currentActiveDelivery != null) {
            val ord = currentActiveDelivery
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFEFF6FF),
              border = androidx.compose.foundation.BorderStroke(1.dp, DeliveryBlue),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "🚚 طلب قيد التوصيل: ${ord.orderNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = DeliveryBlue
                  )
                  Text(
                    text = "${ord.deliveryFee} ر.س أجرة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SuccessGreen
                  )
                }

                Text(
                  text = "من: ${ord.storeName} ➔ إلى: ${ord.customerName} (${ord.customerAddress})",
                  fontSize = 11.sp,
                  color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  // Call customer button
                  OutlinedButton(
                    onClick = {
                      val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ord.customerPhone}"))
                      context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = MintGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اتصال بالزبون", fontSize = 11.sp)
                  }

                  if (ord.status == OrderStatus.ACCEPTED_BY_COURIER) {
                    Button(
                      onClick = {
                        repository.markOrderPickedUp(ord.id)
                        onShowSnackbar("تم تأكيد الاستلام من المحل، توجه للزبون!")
                      },
                      modifier = Modifier.weight(1.2f),
                      colors = ButtonDefaults.buttonColors(containerColor = DeliveryBlue),
                      contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Text("تم الاستلام من المحل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  } else if (ord.status == OrderStatus.PICKED_UP_FROM_STORE) {
                    Button(
                      onClick = {
                        repository.markOrderDelivered(ord.id)
                        onShowSnackbar("أحسنت! تم تسليم الطلب وحساب أجرة ${ord.deliveryFee} ر.س في محفظتك.")
                      },
                      modifier = Modifier.weight(1.2f),
                      colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                      contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Text("تم التسليم للزبون ✅", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          } else {
            // Show list of available orders to select or review
            LazyColumn(
              modifier = Modifier.height(110.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(deliveryOrders) { ord ->
                val isSelected = selectedOrderForRoute?.id == ord.id
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) AmberPrimary.copy(alpha = 0.1f) else Color(0xFFF8FAFC))
                    .clickable { selectedOrderForRoute = ord }
                    .padding(8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text("${ord.orderNumber} • ${ord.category.labelArabic}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("من: ${ord.storeName} ➔ إلى: ${ord.customerName}", fontSize = 10.sp, color = Color(0xFF64748B))
                  }
                  Text(
                    text = ord.status.labelArabic,
                    fontSize = 10.sp,
                    color = when (ord.status) {
                      OrderStatus.DELIVERED -> SuccessGreen
                      OrderStatus.NEW_DISPATCH -> AmberDark
                      else -> DeliveryBlue
                    },
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    }

    // 5. REQUIREMENT: "وثنبث له لفت بطلب توصيلةمن محل الى زبون ونوع طلب ومكان عميل مع رقم لتواصل معه"
    // Floating pop-up alert banner for incoming dispatch delivery orders
    AnimatedVisibility(
      visible = activeAlertOrder != null && isOnline,
      enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
      exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .testTag("courier_dispatch_banner")
    ) {
      if (activeAlertOrder != null) {
        val order = activeAlertOrder
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp)),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // High contrast dark floating dispatch card
          elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // Header: Flash notification badge & dismiss
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = CircleShape,
                  color = AmberPrimary,
                  modifier = Modifier.size(32.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.NotificationsActive,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "🚨 طلب توصيلة جديد متاح الآن!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color.White
                  )
                  Text(
                    text = "أجرة التوصيل المباشرة: ${order.deliveryFee} ر.س",
                    fontSize = 11.sp,
                    color = Color(0xFFFFCC80),
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              IconButton(
                onClick = { repository.dismissCourierAlert() },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF94A3B8))
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Box: من المحل، الى الزبون، نوع الطلب، مكان العميل، رقم التواصل
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF334155),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                // 1. من محل
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("من المحل: ", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                  Text(
                    text = "${order.storeName} (${order.storeLocation?.districtName ?: "محل تجاري"})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }

                // 2. نوع الطلب
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("نوع الطلب: ", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                  Text(
                    text = "${order.category.iconEmoji} ${order.category.labelArabic} • ${order.itemsSummary}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF80CBC4),
                    maxLines = 1
                  )
                }

                // 3. مكان العميل
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("مكان العميل: ", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                  Text(
                    text = "${order.customerLocation.districtName} - ${order.customerAddress}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                  )
                }

                // 4. رقم للتواصل معه
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF4FC3F7), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("رقم العميل للتواصل: ", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Text(
                      text = order.customerPhone,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = Color(0xFF81D4FA)
                    )
                  }

                  // Quick Call Button
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MintGreen,
                    modifier = Modifier
                      .clickable {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                        context.startActivity(intent)
                      }
                      .testTag("courier_call_customer_btn")
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("اتصال", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: قبول الطلب
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = { repository.dismissCourierAlert() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("تجاهل الآن", color = Color(0xFFCBD5E1), fontSize = 12.sp)
              }

              Button(
                onClick = {
                  repository.acceptDeliveryOrder(order.id)
                  selectedOrderForRoute = order
                  onShowSnackbar("تم قبول الطلب بنجاح! تم فتح المسار إلى المحل.")
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                modifier = Modifier
                  .weight(2f)
                  .testTag("accept_delivery_dispatch_btn"),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("قبول طلب التوصيلة الآن", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }
      }
    }
  }
}
