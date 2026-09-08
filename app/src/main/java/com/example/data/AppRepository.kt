package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppRepository {

  private val _stores = MutableStateFlow(SampleData.initialStores)
  val stores: StateFlow<List<Store>> = _stores.asStateFlow()

  private val _products = MutableStateFlow(SampleData.initialProducts)
  val products: StateFlow<List<ProductOrService>> = _products.asStateFlow()

  private val _reviews = MutableStateFlow(SampleData.initialReviews)
  val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

  private val _deliveryOrders = MutableStateFlow(SampleData.initialDeliveryOrders)
  val deliveryOrders: StateFlow<List<DeliveryOrder>> = _deliveryOrders.asStateFlow()

  private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
  val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

  private val _courier = MutableStateFlow(CourierProfile())
  val courier: StateFlow<CourierProfile> = _courier.asStateFlow()

  // Currently managed store by owner
  private val _currentOwnerStoreId = MutableStateFlow(SampleData.initialStores.first().id)
  val currentOwnerStoreId: StateFlow<String> = _currentOwnerStoreId.asStateFlow()

  // Active popup alert for courier (when a new dispatch order arrives)
  private val _activeCourierAlertOrder = MutableStateFlow<DeliveryOrder?>(SampleData.initialDeliveryOrders.firstOrNull())
  val activeCourierAlertOrder: StateFlow<DeliveryOrder?> = _activeCourierAlertOrder.asStateFlow()

  fun selectOwnerStore(storeId: String) {
    _currentOwnerStoreId.value = storeId
  }

  // --- Store Operations ---
  fun createStore(
    name: String,
    ownerName: String,
    type: StoreType,
    location: GeoPoint?,
    phone: String,
    category: ItemCategory,
    description: String,
    bannerEmoji: String
  ): Store {
    val newStore = Store(
      id = "store_${System.currentTimeMillis()}",
      name = name,
      ownerName = ownerName,
      type = type,
      location = if (type == StoreType.PHYSICAL_STORE) location else null,
      phone = phone,
      rating = 5.0f,
      ratingsCount = 1,
      category = category,
      description = description,
      bannerEmoji = bannerEmoji
    )
    _stores.update { it + newStore }
    _currentOwnerStoreId.value = newStore.id
    return newStore
  }

  // --- Product & Inventory Operations ---
  fun updateStock(productId: String, delta: Int) {
    _products.update { list ->
      list.map { product ->
        if (product.id == productId) {
          val newQty = (product.stockQuantity + delta).coerceAtLeast(0)
          product.copy(stockQuantity = newQty, isAvailable = newQty > 0)
        } else {
          product
        }
      }
    }
  }

  fun toggleProductAvailability(productId: String) {
    _products.update { list ->
      list.map { product ->
        if (product.id == productId) {
          product.copy(isAvailable = !product.isAvailable)
        } else {
          product
        }
      }
    }
  }

  fun publishProductOrService(
    storeId: String,
    storeName: String,
    name: String,
    category: ItemCategory,
    price: Double,
    initialStock: Int,
    isService: Boolean,
    description: String,
    iconEmoji: String,
    prepMinutes: Int
  ) {
    val newItem = ProductOrService(
      id = "item_${System.currentTimeMillis()}",
      storeId = storeId,
      storeName = storeName,
      name = name,
      category = category,
      price = price,
      stockQuantity = initialStock,
      isService = isService,
      isAvailable = true,
      description = description,
      rating = 5.0f,
      ratingsCount = 1,
      orderCount = 0,
      estimatedMinutes = prepMinutes,
      iconEmoji = iconEmoji
    )
    _products.update { it + newItem }
  }

  // --- Consumer Cart & Ordering ---
  fun addToCart(product: ProductOrService) {
    _cart.update { currentCart ->
      val existing = currentCart.find { it.product.id == product.id }
      if (existing != null) {
        currentCart.map {
          if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
        }
      } else {
        currentCart + CartItem(product = product, quantity = 1)
      }
    }
  }

  fun removeFromCart(productId: String) {
    _cart.update { currentCart ->
      val existing = currentCart.find { it.product.id == productId }
      if (existing != null && existing.quantity > 1) {
        currentCart.map {
          if (it.product.id == productId) it.copy(quantity = it.quantity - 1) else it
        }
      } else {
        currentCart.filterNot { it.product.id == productId }
      }
    }
  }

  fun clearCart() {
    _cart.value = emptyList()
  }

  fun checkoutOrder(
    customerName: String,
    customerPhone: String,
    customerAddress: String,
    customerLocation: GeoPoint
  ): DeliveryOrder? {
    val cartItems = _cart.value
    if (cartItems.isEmpty()) return null

    val firstProduct = cartItems.first().product
    val store = _stores.value.find { it.id == firstProduct.storeId }
    val totalAmount = cartItems.sumOf { it.product.price * it.quantity }
    val deliveryFee = if (firstProduct.isService) 0.0 else 15.0
    val summary = cartItems.joinToString(" + ") { "${it.quantity}x ${it.product.name}" }

    // Increment orderCount and decrement stock
    _products.update { prods ->
      prods.map { prod ->
        val inCart = cartItems.find { it.product.id == prod.id }
        if (inCart != null) {
          val remaining = (prod.stockQuantity - inCart.quantity).coerceAtLeast(0)
          prod.copy(
            stockQuantity = remaining,
            orderCount = prod.orderCount + inCart.quantity,
            isAvailable = remaining > 0 || prod.isService
          )
        } else {
          prod
        }
      }
    }

    val newOrder = DeliveryOrder(
      id = "order_${System.currentTimeMillis()}",
      orderNumber = "ORD-#${(1000..9999).random()}",
      storeId = firstProduct.storeId,
      storeName = firstProduct.storeName,
      storePhone = store?.phone ?: "0500000000",
      storeLocation = store?.location,
      customerName = customerName,
      customerPhone = customerPhone,
      customerAddress = customerAddress,
      customerLocation = customerLocation,
      itemsSummary = summary,
      category = firstProduct.category,
      totalAmount = totalAmount,
      deliveryFee = deliveryFee,
      status = OrderStatus.NEW_DISPATCH
    )

    _deliveryOrders.update { listOf(newOrder) + it }
    // Dispatch instant alert to couriers!
    _activeCourierAlertOrder.value = newOrder
    clearCart()
    return newOrder
  }

  // --- Review & Rating ---
  fun addReview(targetId: String, userName: String, rating: Int, comment: String) {
    val review = Review(
      id = "rev_${System.currentTimeMillis()}",
      targetId = targetId,
      userName = userName,
      rating = rating,
      comment = comment,
      createdAtText = "الآن"
    )
    _reviews.update { listOf(review) + it }

    // Recalculate product rating if applicable
    _products.update { list ->
      list.map { prod ->
        if (prod.id == targetId) {
          val newCount = prod.ratingsCount + 1
          val newRating = ((prod.rating * prod.ratingsCount) + rating) / newCount
          prod.copy(rating = (newRating * 10).toInt() / 10.0f, ratingsCount = newCount)
        } else {
          prod
        }
      }
    }

    // Recalculate store rating if applicable
    _stores.update { list ->
      list.map { store ->
        if (store.id == targetId) {
          val newCount = store.ratingsCount + 1
          val newRating = ((store.rating * store.ratingsCount) + rating) / newCount
          store.copy(rating = (newRating * 10).toInt() / 10.0f, ratingsCount = newCount)
        } else {
          store
        }
      }
    }
  }

  // --- Courier Actions ---
  fun dismissCourierAlert() {
    _activeCourierAlertOrder.value = null
  }

  fun acceptDeliveryOrder(orderId: String) {
    _deliveryOrders.update { list ->
      list.map { order ->
        if (order.id == orderId) {
          order.copy(status = OrderStatus.ACCEPTED_BY_COURIER, courierName = _courier.value.name)
        } else {
          order
        }
      }
    }
    _activeCourierAlertOrder.value = null
  }

  fun markOrderPickedUp(orderId: String) {
    _deliveryOrders.update { list ->
      list.map { order ->
        if (order.id == orderId) {
          order.copy(status = OrderStatus.PICKED_UP_FROM_STORE)
        } else {
          order
        }
      }
    }
  }

  fun markOrderDelivered(orderId: String) {
    var feeEarned = 0.0
    _deliveryOrders.update { list ->
      list.map { order ->
        if (order.id == orderId) {
          feeEarned = order.deliveryFee
          order.copy(status = OrderStatus.DELIVERED)
        } else {
          order
        }
      }
    }
    _courier.update {
      it.copy(
        completedDeliveries = it.completedDeliveries + 1,
        totalEarnings = it.totalEarnings + feeEarned
      )
    }
  }
}
