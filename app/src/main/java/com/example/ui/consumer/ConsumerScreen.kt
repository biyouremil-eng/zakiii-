package com.example.ui.consumer

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.data.CartItem
import com.example.data.GeoPoint
import com.example.data.ItemCategory
import com.example.data.ProductOrService
import com.example.data.Store
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.MintGreen
import com.example.ui.theme.StarGold
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumerScreen(
  repository: AppRepository,
  stores: List<Store>,
  products: List<ProductOrService>,
  cart: List<CartItem>,
  onShowSnackbar: (String) -> Unit
) {
  var activeViewTab by remember { mutableIntStateOf(0) } // 0: المنتجات والخدمات, 1: خريطة المحلات المحيطة
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(ItemCategory.ALL) }
  var showCartSheet by remember { mutableStateOf(false) }
  var ratingTargetItem by remember { mutableStateOf<ProductOrService?>(null) }
  var ratingTargetStore by remember { mutableStateOf<Store?>(null) }

  val totalCartCount = cart.sumOf { it.quantity }
  val totalCartAmount = cart.sumOf { it.product.price * it.quantity }

  // 1. Most Ordered / Trending Items (Top order count)
  val mostOrderedItems = remember(products) {
    products.sortedByDescending { it.orderCount }.take(6)
  }

  // 2. Filtered & Sorted Search Results:
  // Requirement: "عند بحث يظهر خادمات أكثر تقيمة ثم تتدرج حتى أدنى تقيم" (Ranked by rating descending!)
  val filteredProducts = remember(products, searchQuery, selectedCategory) {
    products.filter { item ->
      val matchesCategory = selectedCategory == ItemCategory.ALL || item.category == selectedCategory
      val matchesQuery = searchQuery.isBlank() ||
        item.name.contains(searchQuery, ignoreCase = true) ||
        item.description.contains(searchQuery, ignoreCase = true) ||
        item.storeName.contains(searchQuery, ignoreCase = true)
      matchesCategory && matchesQuery
    }.sortedByDescending { it.rating } // Strictly descending by rating from highest to lowest!
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF8F9FA))
    ) {
      // Top Search and Location Header
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Location and Cart Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Column {
                Text("التوصيل إلى:", fontSize = 10.sp, color = Color(0xFF64748B))
                Text("الرياض - حي العليا (موقعك الحالي)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
              }
            }

            // Cart Button with Badge
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = AmberPrimary,
              modifier = Modifier
                .clickable { showCartSheet = true }
                .testTag("consumer_cart_button")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                if (totalCartCount > 0) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "$totalCartCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Search Bar
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث عن خدمة خاصة، مواد غذائية، وجبة...", fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = null, tint = AmberPrimary)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "مسح")
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("consumer_search_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
          )
        }
      }

      // View Switcher Tabs: 0: الطلبات والخدمات, 1: خريطة المحلات المحيطة
      TabRow(
        selectedTabIndex = activeViewTab,
        containerColor = Color.White,
        contentColor = AmberPrimary,
        modifier = Modifier.padding(horizontal = 14.dp)
      ) {
        Tab(
          selected = activeViewTab == 0,
          onClick = { activeViewTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("الطلبات والخدمات", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("consumer_tab_items")
        )
        Tab(
          selected = activeViewTab == 1,
          onClick = { activeViewTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("خريطة المحلات المحيطة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("consumer_tab_map")
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Content based on selected tab
      if (activeViewTab == 0) {
        // TAB 0: Items & Services with Top-Ordered Section upfront
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 80.dp)
        ) {
          // Category chips
          item {
            LazyRow(
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(ItemCategory.values()) { cat ->
                FilterChip(
                  selected = selectedCategory == cat,
                  onClick = { selectedCategory = cat },
                  label = { Text("${cat.iconEmoji} ${cat.labelArabic}", fontSize = 12.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmberPrimary.copy(alpha = 0.15f),
                    selectedLabelColor = AmberPrimary
                  )
                )
              }
            }
          }

          // 1. Upfront Section: "الأكثر طلباً" (Most Ordered Upfront - User Requirement)
          if (searchQuery.isBlank()) {
            item {
              Column(modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      Icons.Default.TrendingUp,
                      contentDescription = null,
                      tint = AmberPrimary,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "الطلبات الأكثر طلباً في المقدمة 🔥",
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 15.sp,
                      color = Color(0xFF1E293B)
                    )
                  }
                  Text(
                    text = "الأعلى إقبالاً",
                    fontSize = 11.sp,
                    color = AmberDark,
                    fontWeight = FontWeight.Bold
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                  contentPadding = PaddingValues(horizontal = 14.dp),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  items(mostOrderedItems) { topItem ->
                    Card(
                      modifier = Modifier
                        .width(200.dp)
                        .testTag("trending_item_${topItem.id}"),
                      shape = RoundedCornerShape(14.dp),
                      colors = CardDefaults.cardColors(containerColor = Color.White),
                      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                      Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Text(
                            text = topItem.iconEmoji,
                            fontSize = 28.sp,
                            modifier = Modifier
                              .background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp))
                              .padding(6.dp)
                          )
                          Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF8E1)
                          ) {
                            Row(
                              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                              verticalAlignment = Alignment.CenterVertically
                            ) {
                              Icon(Icons.Default.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(12.dp))
                              Spacer(modifier = Modifier.width(2.dp))
                              Text("${topItem.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                          }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                          text = topItem.name,
                          fontWeight = FontWeight.Bold,
                          fontSize = 13.sp,
                          maxLines = 1,
                          color = Color(0xFF1E293B)
                        )
                        Text(
                          text = topItem.storeName,
                          fontSize = 11.sp,
                          color = Color(0xFF64748B),
                          maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = "🔥 طلب ${topItem.orderCount} مرة",
                          fontSize = 11.sp,
                          color = AmberDark,
                          fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Text(
                            text = "${topItem.price} ر.س",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                          )

                          Button(
                            onClick = {
                              repository.addToCart(topItem)
                              onShowSnackbar("تمت إضافة ${topItem.name} إلى السلة")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_top_item_${topItem.id}")
                          ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("أضف", fontSize = 11.sp)
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          // 2. Search & Filtered List: Ordered by Rating Descending!
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = if (searchQuery.isNotBlank()) "نتائج البحث: الأكثر تقييماً أولاً ⭐" else "جميع المنتجات والخدمات (مرتبة حسب التقييم) ⭐",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color(0xFF1E293B)
                )
                Text(
                  text = "تتدرج من التقييم الأعلى حتى الأدنى (${filteredProducts.size} عنصر)",
                  fontSize = 11.sp,
                  color = Color(0xFF64748B)
                )
              }
            }
          }

          items(filteredProducts) { item ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 5.dp)
                .testTag("consumer_product_card_${item.id}"),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Top
                ) {
                  Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = item.iconEmoji,
                      fontSize = 28.sp,
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
                        text = item.storeName,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                      )
                      Text(
                        text = "${item.price} ر.س • ${if (item.isService) "خدمة حرة" else "منتج متوفر"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberDark
                      )
                    }
                  }

                  // Rating Badge (Prominently shown as requested)
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFF8E1),
                    modifier = Modifier.clickable {
                      ratingTargetItem = item
                    }
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(Icons.Default.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(3.dp))
                      Text(
                        text = "${item.rating} ★",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = Color(0xFFB45309)
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = item.description,
                  fontSize = 12.sp,
                  color = Color(0xFF475569),
                  maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Button to write/give review
                  OutlinedButton(
                    onClick = { ratingTargetItem = item },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("rate_item_btn_${item.id}")
                  ) {
                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تقييم الخدمة", fontSize = 11.sp, color = AmberPrimary)
                  }

                  // Add to Cart / Order Button
                  Button(
                    onClick = {
                      repository.addToCart(item)
                      onShowSnackbar("تمت إضافة ${item.name} إلى سلتك")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_to_cart_btn_${item.id}")
                  ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("طلب الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      } else {
        // TAB 1: Dedicated Surrounding Stores Interactive Map View
        // Requirement: "يملك صفحة خاص تضم خريط تضم في محالات محطة به وخدماتها وتقيمها"
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
        ) {
          Text(
            text = "المحلات المحيطة بك وخدماتها وتقييماتها 📍",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF1E293B)
          )
          Text(
            text = "استكشف المحلات القريبة منك على الخريطة مع استعراض تقييمها وخدماتها مباشرة",
            fontSize = 11.sp,
            color = Color(0xFF64748B)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Map Component (Takes top half)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(280.dp)
              .clip(RoundedCornerShape(16.dp))
              .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
          ) {
            InteractiveMapCanvas(
              modifier = Modifier.fillMaxSize(),
              stores = stores,
              onStoreSelected = { store ->
                // User can select store directly
                ratingTargetStore = store
              }
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "قائمة المحلات المحيطة والخدمات المتوفرة:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
          )

          Spacer(modifier = Modifier.height(6.dp))

          // List of nearby stores with their services and ratings
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 70.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(stores) { st ->
              val storeProductsList = products.filter { it.storeId == st.id }
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("nearby_store_${st.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(st.bannerEmoji, fontSize = 24.sp)
                      Spacer(modifier = Modifier.width(8.dp))
                      Column {
                        Text(st.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                          text = st.location?.districtName ?: "خدمة حرة متنقلة",
                          fontSize = 11.sp,
                          color = Color(0xFF64748B)
                        )
                      }
                    }

                    // Rating
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = Color(0xFFFFF8E1)
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("${st.rating} (${st.ratingsCount})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(6.dp))
                  Text(st.description, fontSize = 11.sp, color = Color(0xFF475569))

                  // Services offered summary
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "الخدمات والمنتجات: ${storeProductsList.joinToString(" • ") { it.name }}",
                    fontSize = 11.sp,
                    color = MintGreen,
                    fontWeight = FontWeight.Medium
                  )

                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    OutlinedButton(
                      onClick = { ratingTargetStore = st },
                      shape = RoundedCornerShape(8.dp),
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                      Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = StarGold)
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("تقييم المتجر", fontSize = 11.sp)
                    }

                    Button(
                      onClick = {
                        // Switch to products tab filtered by this store
                        searchQuery = st.name
                        activeViewTab = 0
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                      shape = RoundedCornerShape(8.dp),
                      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                      Text("عرض القائمة والطلب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Floating Cart Summary Bar (When cart is not empty)
    if (totalCartCount > 0 && !showCartSheet) {
      Card(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(14.dp)
          .clickable { showCartSheet = true }
          .testTag("floating_cart_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AmberPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("$totalCartCount عناصر في السلة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
              Text("الإجمالي: ${"%.1f".format(totalCartAmount)} ر.س", color = Color(0xFFFFF3E0), fontSize = 11.sp)
            }
          }

          Text(
            text = "إتمام الطلب 👈",
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            fontSize = 13.sp
          )
        }
      }
    }
  }

  // --- Modal Bottom Sheet: Cart & Checkout ---
  if (showCartSheet) {
    CartCheckoutSheet(
      cart = cart,
      onDismiss = { showCartSheet = false },
      onRemoveItem = { repository.removeFromCart(it) },
      onCheckout = { custName, custPhone, address ->
        val order = repository.checkoutOrder(
          customerName = custName,
          customerPhone = custPhone,
          customerAddress = address,
          customerLocation = GeoPoint(24.7170, 46.6710, address, "حي العليا")
        )
        showCartSheet = false
        if (order != null) {
          onShowSnackbar("تم إرسال طلبك (${order.orderNumber}) بنجاح وبثه لمندوبي التوصيل!")
        }
      }
    )
  }

  // --- Rating Dialog for Product/Service ---
  if (ratingTargetItem != null) {
    val item = ratingTargetItem!!
    RatingDialog(
      title = "تقييم ${item.name}",
      subtitle = "رأيك يهمنا لمساعدة المستهلكين الآخرين",
      onDismiss = { ratingTargetItem = null },
      onSubmitRating = { stars, comment, name ->
        repository.addReview(item.id, name, stars, comment)
        ratingTargetItem = null
        onShowSnackbar("شكراً لك! تم تسجيل تقييمك ($stars نجوم) بنجاح.")
      }
    )
  }

  // --- Rating Dialog for Store ---
  if (ratingTargetStore != null) {
    val store = ratingTargetStore!!
    RatingDialog(
      title = "تقييم متجر ${store.name}",
      subtitle = "قيّم تجربتك مع هذا المتجر وخدماته",
      onDismiss = { ratingTargetStore = null },
      onSubmitRating = { stars, comment, name ->
        repository.addReview(store.id, name, stars, comment)
        ratingTargetStore = null
        onShowSnackbar("تم تسجيل تقييمك لمتجر ${store.name} بنجاح!")
      }
    )
  }
}

// Dialog to submit star rating and review
@Composable
fun RatingDialog(
  title: String,
  subtitle: String,
  onDismiss: () -> Unit,
  onSubmitRating: (stars: Int, comment: String, userName: String) -> Unit
) {
  var stars by remember { mutableIntStateOf(5) }
  var comment by remember { mutableStateOf("") }
  var userName by remember { mutableStateOf("سالم") }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          onSubmitRating(stars, comment.ifBlank { "خدمة ممتازة ومطابقة للمواصفات" }, userName.ifBlank { "عميل" })
        },
        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
        modifier = Modifier.testTag("submit_rating_confirm_btn")
      ) {
        Text("إرسال التقييم", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
    },
    title = {
      Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(subtitle, fontSize = 12.sp, color = Color(0xFF64748B))

        // Star Rating Selector
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 1..5) {
            IconButton(
              onClick = { stars = i },
              modifier = Modifier.size(38.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "$i نجوم",
                tint = if (i <= stars) StarGold else Color(0xFFCBD5E1),
                modifier = Modifier.size(32.dp)
              )
            }
          }
        }

        Text(
          text = when (stars) {
            5 -> "ممتاز جداً ⭐⭐⭐⭐⭐"
            4 -> "جيد جداً ⭐⭐⭐⭐"
            3 -> "جيد ⭐⭐⭐"
            2 -> "مقبول ⭐⭐"
            else -> "يحتاج تحسين ⭐"
          },
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = AmberDark
        )

        OutlinedTextField(
          value = userName,
          onValueChange = { userName = it },
          label = { Text("اسمك") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = comment,
          onValueChange = { comment = it },
          label = { Text("اكتب رأيك بالتفصيل...") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3
        )
      }
    }
  )
}

// Cart & Checkout Sheet
@Composable
fun CartCheckoutSheet(
  cart: List<CartItem>,
  onDismiss: () -> Unit,
  onRemoveItem: (String) -> Unit,
  onCheckout: (custName: String, custPhone: String, address: String) -> Unit
) {
  var customerName by remember { mutableStateOf("عبدالرحمن البراك") }
  var customerPhone by remember { mutableStateOf("0567788990") }
  var customerAddress by remember { mutableStateOf("عمارة اليرموك - شقة 14 - شارع موسى بن نصير، حي العليا") }

  val subtotal = cart.sumOf { it.product.price * it.quantity }
  val deliveryFee = if (cart.any { !it.product.isService }) 15.0 else 0.0
  val total = subtotal + deliveryFee

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          if (customerName.isNotBlank() && customerPhone.isNotBlank()) {
            onCheckout(customerName, customerPhone, customerAddress)
          }
        },
        enabled = cart.isNotEmpty() && customerName.isNotBlank() && customerPhone.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
        modifier = Modifier.testTag("confirm_order_btn")
      ) {
        Text("تأكيد وبث الطلب للمندوب (${"%.1f".format(total)} ر.س)", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("إغلاق") }
    },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = AmberPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("سلة الطلبات وإتمام الشراء", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Cart Items
        item {
          Text("العناصر المطلوبة (${cart.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        items(cart) { item ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(item.product.iconEmoji, fontSize = 18.sp)
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(item.product.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${item.quantity} × ${item.product.price} ر.س", fontSize = 11.sp, color = Color(0xFF64748B))
              }
            }
            IconButton(
              onClick = { onRemoveItem(item.product.id) },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
            }
          }
        }

        // Summary details
        item {
          Spacer(modifier = Modifier.height(4.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("المجموع الفرعي:", fontSize = 12.sp, color = Color(0xFF64748B))
            Text("${"%.1f".format(subtotal)} ر.س", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("رسوم التوصيل السريع:", fontSize = 12.sp, color = Color(0xFF64748B))
            Text("${"%.1f".format(deliveryFee)} ر.س", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeliveryBlue)
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("الإجمالي الكلي:", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text("${"%.1f".format(total)} ر.س", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AmberDark)
          }
        }

        // Customer Delivery Info
        item {
          Spacer(modifier = Modifier.height(6.dp))
          Text("معلومات التوصيل والتواصل:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
          OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("اسم المستلم") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          OutlinedTextField(
            value = customerPhone,
            onValueChange = { customerPhone = it },
            label = { Text("رقم الجوال للتواصل") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          OutlinedTextField(
            value = customerAddress,
            onValueChange = { customerAddress = it },
            label = { Text("عنوان التوصيل والحي") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
          )
        }
      }
    }
  )
}
