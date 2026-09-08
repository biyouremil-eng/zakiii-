package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRepository
import com.example.data.GeoPoint
import com.example.data.ItemCategory
import com.example.data.StoreType
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.MintGreen

enum class UserRole(val titleArabic: String, val emoji: String, val description: String) {
  MERCHANT("صاحب متجر / تاجر", "🏬", "متابعة المخزون، بث المنتجات أو الخدمات، ومراقبة الأرباح والتقييمات"),
  CONSUMER("مستهلك", "🛒", "تصفح الأكثر طلباً، البحث المصنف بالتقييم، خريطة المحلات والطلب المباشر"),
  COURIER("موصل / مندوب توصيل", "🛵", "خريطة فورية، لافتة منبثقة لطلبات التوصيل، والتواصل السريع بالزبائن")
}

data class UserSession(
  val name: String,
  val phone: String,
  val role: UserRole,
  val storeName: String? = null
)

@Composable
fun AuthScreen(
  repository: AppRepository,
  onLoginSuccess: (UserSession) -> Unit
) {
  var isRegisterMode by remember { mutableStateOf(true) } // true: إنشاء حساب, false: تسجيل دخول
  var selectedRole by remember { mutableStateOf(UserRole.CONSUMER) }

  var fullName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var showPassword by remember { mutableStateOf(false) }

  // Additional Merchant fields
  var storeName by remember { mutableStateOf("") }
  var merchantStoreType by remember { mutableStateOf(StoreType.PHYSICAL_STORE) }

  // Additional Courier fields
  var vehicleType by remember { mutableStateOf("دراجة نارية") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFC))
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // App Branding Header
      item {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = CircleShape,
          color = AmberPrimary,
          modifier = Modifier.size(68.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text("🛍️", fontSize = 36.sp)
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "سوق وطلبات",
          fontSize = 24.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF1E293B)
        )
        Text(
          text = "منظومة التجارة والتوصيل الموحدة للمواد الغذائية والخدمات",
          fontSize = 12.sp,
          color = Color(0xFF64748B)
        )
      }

      // Tab Switcher: تسجيل حساب جديد vs تسجيل الدخول
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          TabRow(
            selectedTabIndex = if (isRegisterMode) 0 else 1,
            containerColor = Color.White,
            contentColor = AmberPrimary
          ) {
            Tab(
              selected = isRegisterMode,
              onClick = { isRegisterMode = true },
              text = { Text("إنشاء حساب جديد", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
              modifier = Modifier.testTag("auth_tab_register")
            )
            Tab(
              selected = !isRegisterMode,
              onClick = { isRegisterMode = false },
              text = { Text("تسجيل الدخول", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
              modifier = Modifier.testTag("auth_tab_login")
            )
          }
        }
      }

      // Role Selection Cards (User Requirement: "في قائمة تسجيل هي التي تحدد اما تاجر مستهلك موصل")
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "حدد نوع الحساب الخاص بك:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF1E293B)
          )
          Spacer(modifier = Modifier.height(8.dp))

          // 3 Role Cards
          UserRole.values().forEach { role ->
            val isSelected = selectedRole == role
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { selectedRole = role }
                .testTag("role_card_${role.name}"),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                  when (role) {
                    UserRole.MERCHANT -> Color(0xFFFFF7ED)
                    UserRole.CONSUMER -> Color(0xFFF0FDF4)
                    UserRole.COURIER -> Color(0xFFEFF6FF)
                  }
                } else Color.White
              ),
              border = if (isSelected) {
                androidx.compose.foundation.BorderStroke(
                  2.dp,
                  when (role) {
                    UserRole.MERCHANT -> AmberPrimary
                    UserRole.CONSUMER -> MintGreen
                    UserRole.COURIER -> DeliveryBlue
                  }
                )
              } else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
              elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = CircleShape,
                  color = when (role) {
                    UserRole.MERCHANT -> AmberPrimary.copy(alpha = 0.15f)
                    UserRole.CONSUMER -> MintGreen.copy(alpha = 0.15f)
                    UserRole.COURIER -> DeliveryBlue.copy(alpha = 0.15f)
                  },
                  modifier = Modifier.size(46.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(role.emoji, fontSize = 24.sp)
                  }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = role.titleArabic,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                      when (role) {
                        UserRole.MERCHANT -> AmberDark
                        UserRole.CONSUMER -> MintGreen
                        UserRole.COURIER -> DeliveryBlue
                      }
                    } else Color(0xFF1E293B)
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = role.description,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp
                  )
                }

                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = when (role) {
                      UserRole.MERCHANT -> AmberPrimary
                      UserRole.CONSUMER -> MintGreen
                      UserRole.COURIER -> DeliveryBlue
                    },
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Input Fields Form
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text(
              text = if (isRegisterMode) "بيانات الحساب الجديد:" else "بيانات الدخول:",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )

            // Name (if registering)
            if (isRegisterMode) {
              OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("الاسم الكامل") },
                placeholder = {
                  Text(
                    when (selectedRole) {
                      UserRole.MERCHANT -> "مثال: أبو فهد التميمي"
                      UserRole.CONSUMER -> "مثال: ريان الشمري"
                      UserRole.COURIER -> "مثال: الكابتن أحمد السريع"
                    }
                  )
                },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AmberPrimary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
              )

              // If Merchant, ask for store name
              if (selectedRole == UserRole.MERCHANT) {
                OutlinedTextField(
                  value = storeName,
                  onValueChange = { storeName = it },
                  label = { Text("اسم المتجر أو الخدمة المعروضة") },
                  placeholder = { Text("مثال: سوبرماركت البركة / صيانة التكييف") },
                  leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = AmberPrimary) },
                  modifier = Modifier.fillMaxWidth(),
                  singleLine = true
                )
              }
            }

            // Phone number
            OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              label = { Text("رقم الجوال") },
              placeholder = { Text("05xxxxxxxx") },
              leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AmberPrimary) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )

            // Password
            OutlinedTextField(
              value = password,
              onValueChange = { password = it },
              label = { Text("كلمة المرور") },
              visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
              leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberPrimary) },
              trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                  Icon(
                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null
                  )
                }
              },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Primary Submit Button
            Button(
              onClick = {
                val effectiveName = if (fullName.isNotBlank()) fullName else {
                  when (selectedRole) {
                    UserRole.MERCHANT -> "أبو فهد التميمي"
                    UserRole.CONSUMER -> "عبدالرحمن البراك"
                    UserRole.COURIER -> "أحمد السريع"
                  }
                }
                val effectivePhone = if (phone.isNotBlank()) phone else "0551234567"

                // If merchant, automatically create or link store
                if (selectedRole == UserRole.MERCHANT && storeName.isNotBlank()) {
                  repository.createStore(
                    name = storeName,
                    ownerName = effectiveName,
                    type = merchantStoreType,
                    location = if (merchantStoreType == StoreType.PHYSICAL_STORE) {
                      GeoPoint(24.7136, 46.6753, "طريق التحلية", "حي العليا")
                    } else null,
                    phone = effectivePhone,
                    category = ItemCategory.GROCERY,
                    description = "متجر متكامل للمواد والخدمات",
                    bannerEmoji = "🏪"
                  )
                }

                onLoginSuccess(
                  UserSession(
                    name = effectiveName,
                    phone = effectivePhone,
                    role = selectedRole,
                    storeName = if (selectedRole == UserRole.MERCHANT) storeName.ifBlank { "سوبرماركت البركة" } else null
                  )
                )
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = when (selectedRole) {
                  UserRole.MERCHANT -> AmberPrimary
                  UserRole.CONSUMER -> MintGreen
                  UserRole.COURIER -> DeliveryBlue
                }
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("auth_submit_btn")
            ) {
              Text(
                text = if (isRegisterMode) {
                  "تسجيل كـ ${selectedRole.titleArabic} والدخول 🚀"
                } else {
                  "تسجيل الدخول كـ ${selectedRole.titleArabic} 🔑"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Quick Demo Access Section (For instant testing without typing)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "⚡ تجربة فورية بنقرة واحدة (حسابات جاهزة):",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Quick Merchant
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    onLoginSuccess(
                      UserSession("أبو فهد التميمي", "0551122334", UserRole.MERCHANT, "سوبرماركت البركة")
                    )
                  }
                  .testTag("quick_login_merchant")
              ) {
                Column(
                  modifier = Modifier.padding(10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text("🏬", fontSize = 20.sp)
                  Text("دخول كتاجر", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberDark)
                }
              }

              // Quick Consumer
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    onLoginSuccess(
                      UserSession("عبدالرحمن البراك", "0567788990", UserRole.CONSUMER)
                    )
                  }
                  .testTag("quick_login_consumer")
              ) {
                Column(
                  modifier = Modifier.padding(10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text("🛒", fontSize = 20.sp)
                  Text("دخول كمستهلك", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                }
              }

              // Quick Courier
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    onLoginSuccess(
                      UserSession("أحمد السريع", "0501234567", UserRole.COURIER)
                    )
                  }
                  .testTag("quick_login_courier")
              ) {
                Column(
                  modifier = Modifier.padding(10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text("🛵", fontSize = 20.sp)
                  Text("دخول كموصل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeliveryBlue)
                }
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}
