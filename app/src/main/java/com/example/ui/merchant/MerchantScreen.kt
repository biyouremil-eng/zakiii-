package com.example.ui.merchant

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRepository
import com.example.data.GeoPoint
import com.example.data.ItemCategory
import com.example.data.ProductOrService
import com.example.data.Review
import com.example.data.Store
import com.example.data.StoreType
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.MintGreen
import com.example.ui.theme.StarGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningRed

@Composable
fun MerchantScreen(
  repository: AppRepository,
  stores: List<Store>,
  products: List<ProductOrService>,
  reviews: List<Review>,
  currentStoreId: String,
  onShowSnackbar: (String) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var showCreateStoreDialog by remember { mutableStateOf(false) }
  var showPublishItemDialog by remember { mutableStateOf(false) }

  val activeStore = stores.find { it.id == currentStoreId } ?: stores.firstOrNull()
  val storeProducts = products.filter { it.storeId == (activeStore?.id ?: "") }
  val storeReviews = reviews.filter { rev ->
    rev.targetId == (activeStore?.id ?: "") || storeProducts.any { it.id == rev.targetId }
  }

  // Calculate profit and stats
  val totalOrders = storeProducts.sumOf { it.orderCount }
  val totalRevenue = storeProducts.sumOf { it.price * it.orderCount }
  val netProfit = totalRevenue * 0.82 // 82% margin after cost

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8F9FA))
  ) {
    // Header & Store Switcher
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = activeStore?.bannerEmoji ?: "🏬",
              fontSize = 28.sp,
              modifier = Modifier
                .background(Color(0xFFFFF3E0), CircleShape)
                .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = activeStore?.name ?: "متجري",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
              )
              Text(
                text = if (activeStore?.type == StoreType.PHYSICAL_STORE) {
                  "📍 ${activeStore.location?.districtName ?: "محل تجاري ذو مقر"}"
                } else {
                  "🌐 ${activeStore?.type?.labelArabic ?: "خدمة حرة متنقلة"}"
                },
                fontSize = 12.sp,
                color = if (activeStore?.type == StoreType.PHYSICAL_STORE) AmberDark else MintGreen,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Button(
            onClick = { showCreateStoreDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("btn_create_new_store")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("متجر جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }

        // Horizontal List of stores to switch easily
        if (stores.size > 1) {
          Spacer(modifier = Modifier.height(10.dp))
          Text("المتاجر والخدمات الخاصة بك:", fontSize = 11.sp, color = Color(0xFF64748B))
          Spacer(modifier = Modifier.height(6.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(stores) { s ->
              val isSelected = s.id == activeStore?.id
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) AmberPrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AmberPrimary) else null,
                modifier = Modifier.clickable { repository.selectOwnerStore(s.id) }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(s.bannerEmoji, fontSize = 14.sp)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = s.name,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) AmberPrimary else Color(0xFF334155)
                  )
                }
              }
            }
          }
        }
      }
    }

    // Tabs: 0: المخزون والبث, 1: الأرباح والتقييمات
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = Color.White,
      contentColor = AmberPrimary,
      modifier = Modifier.padding(horizontal = 14.dp)
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("متابعة المخزون وبث المنتجات", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        },
        modifier = Modifier.testTag("tab_inventory")
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("الأرباح والتقييمات", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        },
        modifier = Modifier.testTag("tab_earnings")
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Tab 0: Inventory & Broadcast Products
    if (selectedTab == 0) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 14.dp)
      ) {
        // Quick Action Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "قائمة المنتجات والخدمات المعروضة",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1E293B)
            )
            Text(
              text = "${storeProducts.size} عنصر متاح في المتجر",
              fontSize = 12.sp,
              color = Color(0xFF64748B)
            )
          }

          Button(
            onClick = { showPublishItemDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("btn_publish_new_item")
          ) {
            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("بث منتج/خدمة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Stock items list
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(storeProducts) { item ->
            val isLowStock = !item.isService && item.stockQuantity <= 5
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("product_stock_item_${item.id}"),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Top
                ) {
                  Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = item.iconEmoji,
                      fontSize = 24.sp,
                      modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                      )
                      Text(
                        text = "${item.price} ر.س • ${item.category.labelArabic}",
                        fontSize = 12.sp,
                        color = AmberDark,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }

                  // Availability Switch
                  Switch(
                    checked = item.isAvailable,
                    onCheckedChange = {
                      repository.toggleProductAvailability(item.id)
                      onShowSnackbar(if (!item.isAvailable) "تم تفعيل عرض ${item.name}" else "تم إيقاف عرض ${item.name}")
                    },
                    colors = SwitchDefaults.colors(
                      checkedThumbColor = Color.White,
                      checkedTrackColor = MintGreen
                    )
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = item.description,
                  fontSize = 12.sp,
                  color = Color(0xFF64748B),
                  maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stock management controls
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLowStock) {
                      Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningRed,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                      text = if (item.isService) {
                        "سعة الحجوزات اليومية: ${item.stockQuantity}"
                      } else {
                        "المخزون المتوفر: ${item.stockQuantity} قطعة"
                      },
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isLowStock) WarningRed else Color(0xFF334155)
                    )
                  }

                  // Increment/Decrement Buttons
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                      shape = CircleShape,
                      color = Color(0xFFE2E8F0),
                      modifier = Modifier
                        .size(32.dp)
                        .clickable {
                          repository.updateStock(item.id, -1)
                        }
                    ) {
                      Icon(
                        Icons.Default.Remove,
                        contentDescription = "إنقاص",
                        modifier = Modifier.padding(6.dp),
                        tint = Color(0xFF1E293B)
                      )
                    }

                    Text(
                      text = "${item.stockQuantity}",
                      modifier = Modifier.padding(horizontal = 12.dp),
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )

                    Surface(
                      shape = CircleShape,
                      color = AmberPrimary,
                      modifier = Modifier
                        .size(32.dp)
                        .clickable {
                          repository.updateStock(item.id, 1)
                        }
                    ) {
                      Icon(
                        Icons.Default.Add,
                        contentDescription = "زيادة",
                        modifier = Modifier.padding(6.dp),
                        tint = Color.White
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    } else {
      // Tab 1: Profits, Analytics & Reviews
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Summary Revenue Cards
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Card 1: Total Revenue
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text("إجمالي المبيعات", fontSize = 11.sp, color = Color(0xFF8D6E63))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  "${"%.1f".format(totalRevenue)} ر.س",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = AmberDark
                )
                Text("من ${totalOrders} عملية بيع", fontSize = 10.sp, color = Color(0xFF6D4C41))
              }
            }

            // Card 2: Net Profit
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text("صافي الأرباح المقدرة", fontSize = 11.sp, color = Color(0xFF388E3C))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  "${"%.1f".format(netProfit)} ر.س",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = SuccessGreen
                )
                Text("هامش ربح ~82%", fontSize = 10.sp, color = Color(0xFF2E7D32))
              }
            }
          }
        }

        // Store Overall Rating Badge
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("تقييم المتجر العام", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                  "بناءً على ${activeStore?.ratingsCount ?: 0} تقييم من العملاء",
                  fontSize = 12.sp,
                  color = Color(0xFF64748B)
                )
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  "${activeStore?.rating ?: 5.0}",
                  fontSize = 22.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFFB45309)
                )
              }
            }
          }
        }

        // Customer Reviews List
        item {
          Text(
            text = "آراء وتقييمات العملاء (${storeReviews.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
          )
        }

        if (storeReviews.isEmpty()) {
          item {
            Text(
              "لا توجد تقييمات مسجلة حتى الآن لهذا المتجر.",
              fontSize = 12.sp,
              color = Color(0xFF94A3B8),
              modifier = Modifier.padding(vertical = 12.dp)
            )
          }
        } else {
          items(storeReviews) { rev ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(rev.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Row {
                    repeat(rev.rating) {
                      Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = StarGold,
                        modifier = Modifier.size(14.dp)
                      )
                    }
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(rev.comment, fontSize = 12.sp, color = Color(0xFF475569))
                Spacer(modifier = Modifier.height(4.dp))
                Text(rev.createdAtText, fontSize = 10.sp, color = Color(0xFF94A3B8))
              }
            }
          }
        }
      }
    }
  }

  // --- Dialog: Create Store (With conditional Map for Physical vs Freelance) ---
  if (showCreateStoreDialog) {
    CreateStoreModal(
      onDismiss = { showCreateStoreDialog = false },
      onCreateStore = { name, owner, type, loc, phone, cat, desc, emoji ->
        repository.createStore(name, owner, type, loc, phone, cat, desc, emoji)
        showCreateStoreDialog = false
        onShowSnackbar("تم إنشاء $name بنجاح!")
      }
    )
  }

  // --- Dialog: Publish New Product / Service ---
  if (showPublishItemDialog && activeStore != null) {
    PublishItemModal(
      store = activeStore,
      onDismiss = { showPublishItemDialog = false },
      onPublish = { name, cat, price, stock, isServ, desc, emoji, prep ->
        repository.publishProductOrService(
          storeId = activeStore.id,
          storeName = activeStore.name,
          name = name,
          category = cat,
          price = price,
          initialStock = stock,
          isService = isServ,
          description = desc,
          iconEmoji = emoji,
          prepMinutes = prep
        )
        showPublishItemDialog = false
        onShowSnackbar("تم بث $name بنجاح في متجرك!")
      }
    )
  }
}

// Modal for creating store with conditional map
@Composable
fun CreateStoreModal(
  onDismiss: () -> Unit,
  onCreateStore: (
    name: String,
    owner: String,
    type: StoreType,
    location: GeoPoint?,
    phone: String,
    category: ItemCategory,
    description: String,
    bannerEmoji: String
  ) -> Unit
) {
  var storeName by remember { mutableStateOf("") }
  var ownerName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("0551234567") }
  var description by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf(StoreType.PHYSICAL_STORE) }
  var selectedCategory by remember { mutableStateOf(ItemCategory.GROCERY) }
  var selectedEmoji by remember { mutableStateOf("🏪") }
  var pickedLocation by remember {
    mutableStateOf<GeoPoint?>(
      GeoPoint(24.7136, 46.6753, "الموقع المختار على الخريطة", "حي العليا - طريق التحلية")
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          if (storeName.isNotBlank()) {
            onCreateStore(
              storeName,
              ownerName.ifBlank { "المالك" },
              selectedType,
              if (selectedType == StoreType.PHYSICAL_STORE) pickedLocation else null,
              phone,
              selectedCategory,
              description.ifBlank { "متجر متكامل يقدم أفضل المنتجات والخدمات" },
              selectedEmoji
            )
          }
        },
        enabled = storeName.isNotBlank() && (selectedType == StoreType.FREELANCE_SERVICE || pickedLocation != null),
        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
        modifier = Modifier.testTag("submit_create_store_btn")
      ) {
        Text("إنشاء المتجر وبدء العمل", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
    },
    title = {
      Text("إنشاء متجر أو خدمة جديدة", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          OutlinedTextField(
            value = storeName,
            onValueChange = { storeName = it },
            label = { Text("اسم المتجر أو الخدمة *") },
            placeholder = { Text("مثال: تموينات النور / صيانة التكييف السريعة") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          OutlinedTextField(
            value = ownerName,
            onValueChange = { ownerName = it },
            label = { Text("اسم المالك / المزود") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("رقم التواصل") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        // Store Type Selection: Physical Store vs Freelance Service
        item {
          Text("نوع المتجر / النشاط:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Physical Store Option
            Card(
              modifier = Modifier
                .weight(1f)
                .clickable { selectedType = StoreType.PHYSICAL_STORE },
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (selectedType == StoreType.PHYSICAL_STORE) AmberPrimary.copy(alpha = 0.12f) else Color(0xFFF8FAFC)
              ),
              border = if (selectedType == StoreType.PHYSICAL_STORE) androidx.compose.foundation.BorderStroke(1.5.dp, AmberPrimary) else null
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏬", fontSize = 24.sp)
                Text("محل بمقر فعلي", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("يطلب تحديد الموقع", fontSize = 10.sp, color = AmberDark)
              }
            }

            // Freelance Service Option
            Card(
              modifier = Modifier
                .weight(1f)
                .clickable { selectedType = StoreType.FREELANCE_SERVICE },
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (selectedType == StoreType.FREELANCE_SERVICE) MintGreen.copy(alpha = 0.12f) else Color(0xFFF8FAFC)
              ),
              border = if (selectedType == StoreType.FREELANCE_SERVICE) androidx.compose.foundation.BorderStroke(1.5.dp, MintGreen) else null
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔧", fontSize = 24.sp)
                Text("خدمة حرة متنقلة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("لا يطلب موقع جغرافي", fontSize = 10.sp, color = MintGreen)
              }
            }
          }
        }

        // Conditional Location Section (Required only for Physical Store)
        if (selectedType == StoreType.PHYSICAL_STORE) {
          item {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  "تحديد موقع المحل على الخريطة (مطلوب):",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = AmberDark
                )
              }
              Text(
                "انقر على الخريطة لاختيار نقطة موقع المتجر بدقة:",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(200.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
              ) {
                InteractiveMapCanvas(
                  modifier = Modifier.fillMaxSize(),
                  isPickerMode = true,
                  pickedLocation = pickedLocation,
                  onLocationPicked = { loc ->
                    pickedLocation = loc
                  }
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "الموقع المحدد: ${pickedLocation?.districtName ?: "انقر على الخريطة"}",
                fontSize = 11.sp,
                color = SuccessGreen,
                fontWeight = FontWeight.Bold
              )
            }
          }
        } else {
          // Freelance Service Notification (No location required!)
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text("خدمة حرة متنقلة", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF004D40))
                  Text(
                    "وفقاً لطلبك، لا يتطلب هذا النوع موقعاً جغرافياً ثابتاً على الخريطة. يمكنك تقديم الخدمة في موقع العميل أو عن بُعد.",
                    fontSize = 11.sp,
                    color = Color(0xFF00695C)
                  )
                }
              }
            }
          }
        }

        // Category Selection
        item {
          Text("فئة النشاط:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(ItemCategory.GROCERY, ItemCategory.FOOD, ItemCategory.SERVICE, ItemCategory.OTHER).forEach { cat ->
              FilterChip(
                selected = selectedCategory == cat,
                onClick = { selectedCategory = cat },
                label = { Text("${cat.iconEmoji} ${cat.labelArabic}", fontSize = 11.sp) }
              )
            }
          }
        }

        // Emoji Icon Selection
        item {
          Text("رمز المتجر:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("🏪", "🥬", "🍗", "🥐", "❄️", "⚡", "📦", "☕").forEach { em ->
              Surface(
                shape = CircleShape,
                color = if (selectedEmoji == em) AmberPrimary.copy(alpha = 0.2f) else Color(0xFFF1F5F9),
                border = if (selectedEmoji == em) androidx.compose.foundation.BorderStroke(2.dp, AmberPrimary) else null,
                modifier = Modifier
                  .size(38.dp)
                  .clickable { selectedEmoji = em }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(em, fontSize = 18.sp)
                }
              }
            }
          }
        }
      }
    }
  )
}

// Modal for Publishing New Product / Service
@Composable
fun PublishItemModal(
  store: Store,
  onDismiss: () -> Unit,
  onPublish: (
    name: String,
    category: ItemCategory,
    price: Double,
    stock: Int,
    isService: Boolean,
    description: String,
    iconEmoji: String,
    prepMinutes: Int
  ) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var priceText by remember { mutableStateOf("25") }
  var stockText by remember { mutableStateOf("20") }
  var prepMinutesText by remember { mutableStateOf("15") }
  var description by remember { mutableStateOf("") }
  var isService by remember { mutableStateOf(store.type == StoreType.FREELANCE_SERVICE) }
  var selectedCategory by remember { mutableStateOf(store.category) }
  var selectedEmoji by remember { mutableStateOf(if (isService) "🔧" else "🥦") }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            val price = priceText.toDoubleOrNull() ?: 20.0
            val stock = stockText.toIntOrNull() ?: 15
            val prep = prepMinutesText.toIntOrNull() ?: 15
            onPublish(
              name,
              selectedCategory,
              price,
              stock,
              isService,
              description.ifBlank { "منتج/خدمة مميزة متوفرة الآن في $name" },
              selectedEmoji,
              prep
            )
          }
        },
        enabled = name.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
        modifier = Modifier.testTag("submit_publish_item_btn")
      ) {
        Text("بث العنصر في المتجر", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
    },
    title = {
      Text("بث منتج أو خدمة جديدة", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("اسم المنتج أو الخدمة *") },
          placeholder = { Text("مثال: زيتون طازج / وجبة شاورما / صيانة غسالات") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = priceText,
            onValueChange = { priceText = it },
            label = { Text("السعر (ر.س)") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = stockText,
            onValueChange = { stockText = it },
            label = { Text(if (isService) "سعة الحجوزات" else "كمية المخزون") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("الوصف والتفاصيل") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )

        // Choose Emoji
        Text("أيقونة العنصر:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("🥦", "🥕", "🍗", "🍔", "🥐", "🥛", "🫒", "❄️", "⚡", "🔧", "📦").forEach { em ->
            Surface(
              shape = CircleShape,
              color = if (selectedEmoji == em) MintGreen.copy(alpha = 0.2f) else Color(0xFFF1F5F9),
              border = if (selectedEmoji == em) androidx.compose.foundation.BorderStroke(2.dp, MintGreen) else null,
              modifier = Modifier
                .size(34.dp)
                .clickable { selectedEmoji = em }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(em, fontSize = 16.sp)
              }
            }
          }
        }
      }
    }
  )
}
