package com.example.data

enum class StoreType(val labelArabic: String, val requiresLocation: Boolean) {
  PHYSICAL_STORE("محل تجاري ذو مقر فعلي", true),
  FREELANCE_SERVICE("خدمة حرة / متنقلة", false)
}

enum class ItemCategory(val labelArabic: String, val iconEmoji: String) {
  ALL("الكل", "✨"),
  GROCERY("مواد غذائية", "🥦"),
  FOOD("مأكولات ومطاعم", "🍔"),
  SERVICE("خدمات وصيانة", "🔧"),
  OTHER("أخرى", "📦")
}

data class GeoPoint(
  val latitude: Double,
  val longitude: Double,
  val addressName: String,
  val districtName: String
)

data class Store(
  val id: String,
  val name: String,
  val ownerName: String,
  val type: StoreType,
  val location: GeoPoint?,
  val phone: String,
  val rating: Float,
  val ratingsCount: Int,
  val category: ItemCategory,
  val description: String,
  val bannerEmoji: String,
  val isOpen: Boolean = true
)

data class ProductOrService(
  val id: String,
  val storeId: String,
  val storeName: String,
  val name: String,
  val category: ItemCategory,
  val price: Double,
  val stockQuantity: Int,
  val isService: Boolean,
  val isAvailable: Boolean = true,
  val description: String,
  val rating: Float,
  val ratingsCount: Int = 1,
  val orderCount: Int, // عدد مرات الطلب (للأكثر طلباً)
  val estimatedMinutes: Int = 20,
  val iconEmoji: String = "🛒"
)

data class Review(
  val id: String,
  val targetId: String, // storeId or productId
  val userName: String,
  val rating: Int, // 1..5
  val comment: String,
  val createdAtText: String = "اليوم"
)

enum class OrderStatus(val labelArabic: String) {
  NEW_DISPATCH("طلب جديد بانتظار مندوب"),
  ACCEPTED_BY_COURIER("تم قبول الطلب من المندوب"),
  PICKED_UP_FROM_STORE("تم الاستلام من المحل - في الطريق"),
  DELIVERED("تم التسليم للزبون"),
  CANCELLED("ملغي")
}

data class DeliveryOrder(
  val id: String,
  val orderNumber: String,
  val storeId: String,
  val storeName: String,
  val storePhone: String,
  val storeLocation: GeoPoint?,
  val customerName: String,
  val customerPhone: String,
  val customerAddress: String,
  val customerLocation: GeoPoint,
  val itemsSummary: String,
  val category: ItemCategory,
  val totalAmount: Double,
  val deliveryFee: Double,
  val status: OrderStatus,
  val courierName: String? = null,
  val createdAtMillis: Long = System.currentTimeMillis()
)

data class CartItem(
  val product: ProductOrService,
  val quantity: Int
)

data class CourierProfile(
  val name: String = "أحمد السريع",
  val phone: String = "0501234567",
  val location: GeoPoint = GeoPoint(24.7136, 46.6753, "طريق الملك فهد", "حي العليا"),
  val isOnline: Boolean = true,
  val completedDeliveries: Int = 14,
  val totalEarnings: Double = 280.0
)
