package com.example.data

object SampleData {

  val initialStores = listOf(
    Store(
      id = "store_1",
      name = "سوبرماركت البركة للمواد الغذائية",
      ownerName = "أبو فهد التميمي",
      type = StoreType.PHYSICAL_STORE,
      location = GeoPoint(24.7136, 46.6753, "شارع التحلية - تقاطع العليا", "حي العليا"),
      phone = "0551122334",
      rating = 4.8f,
      ratingsCount = 128,
      category = ItemCategory.GROCERY,
      description = "خضار وفواكه طازجة يومية ومنتجات استهلاكية وألبان عضوية.",
      bannerEmoji = "🥬"
    ),
    Store(
      id = "store_2",
      name = "مطعم ومطبخ شواية الأصالة",
      ownerName = "الشيف خالد المنصور",
      type = StoreType.PHYSICAL_STORE,
      location = GeoPoint(24.7200, 46.6850, "شارع العروبة", "حي الورود"),
      phone = "0559988776",
      rating = 4.9f,
      ratingsCount = 245,
      category = ItemCategory.FOOD,
      description = "أشهى المأكولات الشعبية والشواية والمشاوي الطازجة مع الأرز البشاور.",
      bannerEmoji = "🍗"
    ),
    Store(
      id = "store_3",
      name = "مخبز وحلويات الياسمين",
      ownerName = "سالم القرني",
      type = StoreType.PHYSICAL_STORE,
      location = GeoPoint(24.7080, 46.6690, "طريق الأمير سلطان", "حي السليمانية"),
      phone = "0553344556",
      rating = 4.6f,
      ratingsCount = 89,
      category = ItemCategory.FOOD,
      description = "خبز فرنسي ساخن، معجنات طازجة وحلويات شرقية وغربية فاخرة.",
      bannerEmoji = "🥐"
    ),
    Store(
      id = "store_4",
      name = "فني تبريد وتكييف متنقل (خدمة حرة)",
      ownerName = "المهندس طارق العلي",
      type = StoreType.FREELANCE_SERVICE,
      location = null, // خدمة حرة لا تتطلب موقعاً
      phone = "0504433221",
      rating = 4.95f,
      ratingsCount = 76,
      category = ItemCategory.SERVICE,
      description = "صيانة وتنظيف مكيفات سبليت وشباك وتعبئة فريون في موقع العميل مباشرة.",
      bannerEmoji = "❄️"
    ),
    Store(
      id = "store_5",
      name = "كهربائي منازل وسيارات فوري (خدمة حرة)",
      ownerName = "ماجد الأحمدي",
      type = StoreType.FREELANCE_SERVICE,
      location = null, // خدمة حرة
      phone = "0548877665",
      rating = 4.7f,
      ratingsCount = 53,
      category = ItemCategory.SERVICE,
      description = "إصلاح الأعطال الكهربائية وتمديدات الإضاءة الذكية والصيانة الطارئة السريعة.",
      bannerEmoji = "⚡"
    )
  )

  val initialProducts = listOf(
    // المواد الغذائية
    ProductOrService(
      id = "prod_1",
      storeId = "store_1",
      storeName = "سوبرماركت البركة للمواد الغذائية",
      name = "سلة خضار مشكلة طازجة (5 كجم)",
      category = ItemCategory.GROCERY,
      price = 35.0,
      stockQuantity = 42,
      isService = false,
      isAvailable = true,
      description = "طماطم، خيار، كوسة، جزر، وبطاطس طازجة من المزارع المحلية.",
      rating = 4.9f,
      ratingsCount = 112,
      orderCount = 340, // أكثر طلباً
      estimatedMinutes = 15,
      iconEmoji = "🥕"
    ),
    ProductOrService(
      id = "prod_2",
      storeId = "store_1",
      storeName = "سوبرماركت البركة للمواد الغذائية",
      name = "حليب عضوي طازج كامل الدسم (2 لتر)",
      category = ItemCategory.GROCERY,
      price = 14.5,
      stockQuantity = 18,
      isService = false,
      isAvailable = true,
      description = "حليب بقر طازج غني بالكالسيوم مبستر يومياً.",
      rating = 4.8f,
      ratingsCount = 65,
      orderCount = 280, // عالي الطلب
      estimatedMinutes = 10,
      iconEmoji = "🥛"
    ),
    ProductOrService(
      id = "prod_3",
      storeId = "store_1",
      storeName = "سوبرماركت البركة للمواد الغذائية",
      name = "زيت زيتون بكر ممتاز معصور على البارد (1 لتر)",
      category = ItemCategory.GROCERY,
      price = 45.0,
      stockQuantity = 4, // منخفض المخزون للتنبيه
      isService = false,
      isAvailable = true,
      description = "زيت زيتون الجوف نخب أول حموضة أقل من 0.8%.",
      rating = 4.95f,
      ratingsCount = 98,
      orderCount = 210,
      estimatedMinutes = 10,
      iconEmoji = "🫒"
    ),

    // مأكولات ومطاعم
    ProductOrService(
      id = "prod_4",
      storeId = "store_2",
      storeName = "مطعم ومطبخ شواية الأصالة",
      name = "نصف حبة دجاج شواية مع رز بشاور وسلطات",
      category = ItemCategory.FOOD,
      price = 26.0,
      stockQuantity = 60,
      isService = false,
      isAvailable = true,
      description = "دجاج متبل بخلطة الأصالة المشوية على الحطب مع الرز البشاور الفاخر والصوص الحار.",
      rating = 4.9f,
      ratingsCount = 190,
      orderCount = 450, // متصدر الأكثر طلباً!
      estimatedMinutes = 20,
      iconEmoji = "🍗"
    ),
    ProductOrService(
      id = "prod_5",
      storeId = "store_2",
      storeName = "مطعم ومطبخ شواية الأصالة",
      name = "وجبة برجر لحم بلدي مشوي مع بطاطس مقرمشة",
      category = ItemCategory.FOOD,
      price = 32.0,
      stockQuantity = 25,
      isService = false,
      isAvailable = true,
      description = "لحم نعيمي طازج مع جبن شيدر ذائب وصوص التدخين في خبز بريوش طري.",
      rating = 4.75f,
      ratingsCount = 130,
      orderCount = 310,
      estimatedMinutes = 25,
      iconEmoji = "🍔"
    ),
    ProductOrService(
      id = "prod_6",
      storeId = "store_3",
      storeName = "مخبز وحلويات الياسمين",
      name = "كرواسون زبدة فرنسي محشو بالجبن والعسل (4 قطع)",
      category = ItemCategory.FOOD,
      price = 22.0,
      stockQuantity = 3, // قليل المخزون
      isService = false,
      isAvailable = true,
      description = "طبقات مقرمشة وهشة مخبوزة بزبدة لورباك الفاخرة طازجة كل ساعة.",
      rating = 4.7f,
      ratingsCount = 82,
      orderCount = 190,
      estimatedMinutes = 15,
      iconEmoji = "🥐"
    ),

    // خدمات حرة وصيانة
    ProductOrService(
      id = "serv_1",
      storeId = "store_4",
      storeName = "فني تبريد وتكييف متنقل (خدمة حرة)",
      name = "غسيل وصيانة مكيف سبليت في المنزل مع فحص الفريون",
      category = ItemCategory.SERVICE,
      price = 90.0,
      stockQuantity = 10,
      isService = true,
      isAvailable = true,
      description = "تنظيف عميق بالضغط العالي للوحدة الداخلية والخارجية وتعقيم الفلاتر وتعبئة الفريون.",
      rating = 5.0f, // أعلى تقييم
      ratingsCount = 48,
      orderCount = 150,
      estimatedMinutes = 45,
      iconEmoji = "❄️"
    ),
    ProductOrService(
      id = "serv_2",
      storeId = "store_5",
      storeName = "كهربائي منازل وسيارات فوري (خدمة حرة)",
      name = "فحص كهربائي شامل للمنزل وإصلاح القواطع والتماسات",
      category = ItemCategory.SERVICE,
      price = 75.0,
      stockQuantity = 8,
      isService = true,
      isAvailable = true,
      description = "كشف التماسات بجهاز ليزري وإصلاح أعطال الإضاءة وتبديل المفاتيح التالفة بضمان.",
      rating = 4.85f,
      ratingsCount = 39,
      orderCount = 95,
      estimatedMinutes = 40,
      iconEmoji = "⚡"
    ),
    ProductOrService(
      id = "serv_3",
      storeId = "store_4",
      storeName = "فني تبريد وتكييف متنقل (خدمة حرة)",
      name = "صيانة وتغيير كومبريسور الثلاجات المنزلية",
      category = ItemCategory.SERVICE,
      price = 140.0,
      stockQuantity = 5,
      isService = true,
      isAvailable = true,
      description = "فحص تسريب الفريون وتغيير الموتور مع ضمان سنة كاملة في الموقع.",
      rating = 4.6f,
      ratingsCount = 21,
      orderCount = 45,
      estimatedMinutes = 60,
      iconEmoji = "🧊"
    )
  )

  val initialReviews = listOf(
    Review("rev_1", "prod_4", "سلطان القحطاني", 5, "أفضل دجاج شواية في الرياض! الرز نظيف والدجاج محمر بامتياز.", "قبل ساعتين"),
    Review("rev_2", "prod_1", "أم ريان", 5, "الخضار وصلت طازجة جداً ومغلفة بعناية، سأطلب باستمرار.", "قبل 4 ساعات"),
    Review("rev_3", "serv_1", "فيصل الدوسري", 5, "الفني ممتاز ومحترم جداً، نظف المكيفين وعادت البرودة كأنه جديد!", "أمس"),
    Review("rev_4", "store_1", "عبدالله الشهري", 4, "تعامل راقي وتوصيل سريع، بارك الله فيكم.", "قبل يومين"),
    Review("rev_5", "prod_5", "تركي المطيري", 4, "البرجر لذيذ واللحم طري ومتبل صح.", "قبل 3 أيام")
  )

  val initialDeliveryOrders = listOf(
    DeliveryOrder(
      id = "order_101",
      orderNumber = "ORD-#4092",
      storeId = "store_2",
      storeName = "مطعم ومطبخ شواية الأصالة",
      storePhone = "0559988776",
      storeLocation = GeoPoint(24.7200, 46.6850, "شارع العروبة", "حي الورود"),
      customerName = "عبدالرحمن البراك",
      customerPhone = "0567788990",
      customerAddress = "عمارة اليرموك - شقة 14 - شارع موسى بن نصير",
      customerLocation = GeoPoint(24.7170, 46.6710, "شارع موسى بن نصير", "حي العليا"),
      itemsSummary = "2x دجاج شواية مع رز بشاور + 2 بيبسي وسلطات حارة",
      category = ItemCategory.FOOD,
      totalAmount = 64.0,
      deliveryFee = 16.0,
      status = OrderStatus.NEW_DISPATCH
    ),
    DeliveryOrder(
      id = "order_102",
      orderNumber = "ORD-#4093",
      storeId = "store_1",
      storeName = "سوبرماركت البركة للمواد الغذائية",
      storePhone = "0551122334",
      storeLocation = GeoPoint(24.7136, 46.6753, "شارع التحلية", "حي العليا"),
      customerName = "سارة العتيبي",
      customerPhone = "0591239874",
      customerAddress = "فيلا 42 - بجوار مسجد التقوى",
      customerLocation = GeoPoint(24.7250, 46.6800, "شارع التخصصي", "حي الرحمانية"),
      itemsSummary = "سلة خضار مشكلة + 2 حليب عضوي + زيت زيتون نخب أول",
      category = ItemCategory.GROCERY,
      totalAmount = 109.0,
      deliveryFee = 18.0,
      status = OrderStatus.NEW_DISPATCH
    )
  )
}
