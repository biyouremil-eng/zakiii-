package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeliveryOrder
import com.example.data.GeoPoint
import com.example.data.Store
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.MintGreen
import com.example.ui.theme.StarGold

@Composable
fun InteractiveMapCanvas(
  modifier: Modifier = Modifier,
  stores: List<Store> = emptyList(),
  activeOrder: DeliveryOrder? = null,
  courierLocation: GeoPoint? = null,
  userLocation: GeoPoint = GeoPoint(24.7140, 46.6740, "موقعك الحالي", "حي العليا"),
  isPickerMode: Boolean = false,
  pickedLocation: GeoPoint? = null,
  onLocationPicked: ((GeoPoint) -> Unit)? = null,
  onStoreSelected: ((Store) -> Unit)? = null
) {
  var scale by remember { mutableFloatStateOf(1.0f) }
  var offset by remember { mutableStateOf(Offset.Zero) }
  var selectedStore by remember { mutableStateOf<Store?>(null) }

  val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
    scale = (scale * zoomChange).coerceIn(0.6f, 2.5f)
    offset += panChange
  }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseRadius by infiniteTransition.animateFloat(
    initialValue = 18f,
    targetValue = 38f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse_anim"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse_alpha"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF3F5F8))
      .clip(RoundedCornerShape(16.dp))
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .transformable(state = transformableState)
        .pointerInput(isPickerMode) {
          detectTapGestures { tapOffset ->
            if (isPickerMode && onLocationPicked != null) {
              val normX = ((tapOffset.x - offset.x) / (size.width * scale)).coerceIn(0.1f, 0.9f)
              val normY = ((tapOffset.y - offset.y) / (size.height * scale)).coerceIn(0.1f, 0.9f)

              val lat = 24.7000 + (1 - normY) * 0.0300
              val lng = 46.6600 + normX * 0.0300
              val district = when {
                normX < 0.4 && normY < 0.4 -> "حي الورود - تقاطع العروبة"
                normX >= 0.4 && normY < 0.5 -> "حي العليا - شارع التحلية"
                normX < 0.5 && normY >= 0.5 -> "حي السليمانية - طريق سلطان"
                else -> "حي المروج - طريق الملك فهد"
              }
              onLocationPicked(
                GeoPoint(
                  latitude = (lat * 10000).toInt() / 10000.0,
                  longitude = (lng * 10000).toInt() / 10000.0,
                  addressName = "الموقع المختار على الخريطة",
                  districtName = district
                )
              )
            } else {
              // Check if tapped near any store
              val tappedStore = stores.find { store ->
                val loc = store.location ?: return@find false
                val pinNormX = ((loc.longitude - 46.6600) / 0.0300).toFloat().coerceIn(0.1f, 0.9f)
                val pinNormY = (1f - ((loc.latitude - 24.7000) / 0.0300).toFloat()).coerceIn(0.1f, 0.9f)
                val pinX = pinNormX * size.width * scale + offset.x
                val pinY = pinNormY * size.height * scale + offset.y
                val dist = (tapOffset.x - pinX) * (tapOffset.x - pinX) + (tapOffset.y - pinY) * (tapOffset.y - pinY)
                dist < (40f * 40f)
              }
              if (tappedStore != null) {
                selectedStore = tappedStore
                onStoreSelected?.invoke(tappedStore)
              } else {
                selectedStore = null
              }
            }
          }
        }
    ) {
      val w = size.width
      val h = size.height

      // Background parks & zones
      drawRect(color = Color(0xFFE8ECEF))

      // Green zones (gardens/parks)
      val greenArea1 = Path().apply {
        moveTo(w * 0.1f * scale + offset.x, h * 0.15f * scale + offset.y)
        lineTo(w * 0.35f * scale + offset.x, h * 0.12f * scale + offset.y)
        lineTo(w * 0.38f * scale + offset.x, h * 0.32f * scale + offset.y)
        lineTo(w * 0.08f * scale + offset.x, h * 0.28f * scale + offset.y)
        close()
      }
      drawPath(greenArea1, color = Color(0xFFD4EAD6))

      val greenArea2 = Path().apply {
        moveTo(w * 0.65f * scale + offset.x, h * 0.65f * scale + offset.y)
        lineTo(w * 0.92f * scale + offset.x, h * 0.62f * scale + offset.y)
        lineTo(w * 0.95f * scale + offset.x, h * 0.88f * scale + offset.y)
        lineTo(w * 0.70f * scale + offset.x, h * 0.85f * scale + offset.y)
        close()
      }
      drawPath(greenArea2, color = Color(0xFFD4EAD6))

      // Main Arterial Roads (Highways)
      val roadColor = Color(0xFFFFFFFF)
      val roadBorder = Color(0xFFD5DCE2)

      // Horizontal main road 1 (طريق التحلية)
      val y1 = h * 0.40f * scale + offset.y
      drawLine(roadBorder, Offset(0f, y1), Offset(w, y1), strokeWidth = 26f * scale)
      drawLine(roadColor, Offset(0f, y1), Offset(w, y1), strokeWidth = 22f * scale)

      // Horizontal main road 2 (طريق العروبة)
      val y2 = h * 0.75f * scale + offset.y
      drawLine(roadBorder, Offset(0f, y2), Offset(w, y2), strokeWidth = 22f * scale)
      drawLine(roadColor, Offset(0f, y2), Offset(w, y2), strokeWidth = 18f * scale)

      // Vertical main road 1 (طريق الملك فهد)
      val x1 = w * 0.32f * scale + offset.x
      drawLine(roadBorder, Offset(x1, 0f), Offset(x1, h), strokeWidth = 32f * scale)
      drawLine(roadColor, Offset(x1, 0f), Offset(x1, h), strokeWidth = 26f * scale)

      // Vertical main road 2 (طريق التخصصي)
      val x2 = w * 0.68f * scale + offset.x
      drawLine(roadBorder, Offset(x2, 0f), Offset(x2, h), strokeWidth = 24f * scale)
      drawLine(roadColor, Offset(x2, 0f), Offset(x2, h), strokeWidth = 20f * scale)

      // Secondary residential grid
      for (i in 1..5) {
        val secY = (h * (i * 0.16f)) * scale + offset.y
        drawLine(Color(0xFFE2E7EC), Offset(0f, secY), Offset(w, secY), strokeWidth = 8f * scale)
      }
      for (i in 1..5) {
        val secX = (w * (i * 0.17f)) * scale + offset.x
        drawLine(Color(0xFFE2E7EC), Offset(secX, 0f), Offset(secX, h), strokeWidth = 8f * scale)
      }

      // Draw Route Line if active order exists
      if (activeOrder != null) {
        val storeLoc = activeOrder.storeLocation
        val custLoc = activeOrder.customerLocation

        val sX = if (storeLoc != null) {
          (((storeLoc.longitude - 46.6600) / 0.0300).toFloat().coerceIn(0.15f, 0.85f)) * w * scale + offset.x
        } else (w * 0.35f * scale + offset.x)
        val sY = if (storeLoc != null) {
          ((1f - ((storeLoc.latitude - 24.7000) / 0.0300).toFloat()).coerceIn(0.15f, 0.85f)) * h * scale + offset.y
        } else (h * 0.45f * scale + offset.y)

        val cX = (((custLoc.longitude - 46.6600) / 0.0300).toFloat().coerceIn(0.15f, 0.85f)) * w * scale + offset.x
        val cY = ((1f - ((custLoc.latitude - 24.7000) / 0.0300).toFloat()).coerceIn(0.15f, 0.85f)) * h * scale + offset.y

        val routePath = Path().apply {
          moveTo(sX, sY)
          // Waypoint via road intersection
          lineTo(sX, cY)
          lineTo(cX, cY)
        }

        // Outer glow
        drawPath(
          path = routePath,
          color = DeliveryBlue.copy(alpha = 0.3f),
          style = Stroke(width = 14f * scale, cap = StrokeCap.Round)
        )
        // Dashed path line
        drawPath(
          path = routePath,
          color = DeliveryBlue,
          style = Stroke(
            width = 6f * scale,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
          )
        )
      }

      // Draw Store Pins (Only stores that have locations)
      stores.filter { it.location != null }.forEach { store ->
        val loc = store.location!!
        val normX = ((loc.longitude - 46.6600) / 0.0300).toFloat().coerceIn(0.1f, 0.9f)
        val normY = (1f - ((loc.latitude - 24.7000) / 0.0300).toFloat()).coerceIn(0.1f, 0.9f)
        val pinX = normX * w * scale + offset.x
        val pinY = normY * h * scale + offset.y

        val isHighlighted = selectedStore?.id == store.id

        // Pulse around pin
        if (isHighlighted) {
          drawCircle(
            color = AmberPrimary.copy(alpha = pulseAlpha),
            radius = pulseRadius * scale,
            center = Offset(pinX, pinY)
          )
        }

        // Pin shadow
        drawCircle(
          color = Color(0x33000000),
          radius = 16f * scale,
          center = Offset(pinX, pinY + 3f)
        )

        // Store Pin outer circle
        drawCircle(
          color = if (isHighlighted) AmberPrimary else Color(0xFF1E88E5),
          radius = if (isHighlighted) 18f * scale else 15f * scale,
          center = Offset(pinX, pinY)
        )
        drawCircle(
          color = Color.White,
          radius = 7f * scale,
          center = Offset(pinX, pinY)
        )
      }

      // Draw Courier Pin if provided
      if (courierLocation != null) {
        val normX = ((courierLocation.longitude - 46.6600) / 0.0300).toFloat().coerceIn(0.1f, 0.9f)
        val normY = (1f - ((courierLocation.latitude - 24.7000) / 0.0300).toFloat()).coerceIn(0.1f, 0.9f)
        val pinX = normX * w * scale + offset.x
        val pinY = normY * h * scale + offset.y

        // Courier radar pulse
        drawCircle(
          color = MintGreen.copy(alpha = pulseAlpha),
          radius = (pulseRadius + 10f) * scale,
          center = Offset(pinX, pinY)
        )
        drawCircle(
          color = MintGreen,
          radius = 16f * scale,
          center = Offset(pinX, pinY)
        )
        drawCircle(
          color = Color.White,
          radius = 6f * scale,
          center = Offset(pinX, pinY)
        )
      }

      // Draw Customer location / User Location
      val userNormX = ((userLocation.longitude - 46.6600) / 0.0300).toFloat().coerceIn(0.1f, 0.9f)
      val userNormY = (1f - ((userLocation.latitude - 24.7000) / 0.0300).toFloat()).coerceIn(0.1f, 0.9f)
      val userPinX = userNormX * w * scale + offset.x
      val userPinY = userNormY * h * scale + offset.y

      drawCircle(
        color = Color(0xFFE91E63).copy(alpha = 0.25f),
        radius = 24f * scale,
        center = Offset(userPinX, userPinY)
      )
      drawCircle(
        color = Color(0xFFE91E63),
        radius = 12f * scale,
        center = Offset(userPinX, userPinY)
      )
      drawCircle(
        color = Color.White,
        radius = 5f * scale,
        center = Offset(userPinX, userPinY)
      )

      // Draw Picked Location Marker if in picker mode
      if (isPickerMode && pickedLocation != null) {
        val pNormX = ((pickedLocation.longitude - 46.6600) / 0.0300).toFloat().coerceIn(0.1f, 0.9f)
        val pNormY = (1f - ((pickedLocation.latitude - 24.7000) / 0.0300).toFloat()).coerceIn(0.1f, 0.9f)
        val pX = pNormX * w * scale + offset.x
        val pY = pNormY * h * scale + offset.y

        drawCircle(
          color = AmberPrimary.copy(alpha = 0.35f),
          radius = 32f * scale,
          center = Offset(pX, pY)
        )
        drawCircle(
          color = AmberPrimary,
          radius = 18f * scale,
          center = Offset(pX, pY)
        )
        drawCircle(
          color = Color.White,
          radius = 7f * scale,
          center = Offset(pX, pY)
        )
      }
    }

    // Top overlay badge
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.94f),
        shadowElevation = 4.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isPickerMode) Icons.Default.LocationOn else Icons.Default.MyLocation,
            contentDescription = null,
            tint = if (isPickerMode) AmberPrimary else DeliveryBlue,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isPickerMode) {
              pickedLocation?.districtName ?: "انقر على الخريطة لتحديد موقع المتجر"
            } else {
              "خريطة الرياض التفاعلية (المحلات المحيطة)"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
          )
        }
      }

      // Map Controls (Zoom In/Out, Reset)
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
          shape = CircleShape,
          color = Color.White,
          shadowElevation = 3.dp,
          modifier = Modifier
            .size(36.dp)
            .clickable { scale = (scale * 1.25f).coerceAtMost(2.5f) }
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "تكبير",
            modifier = Modifier.padding(8.dp),
            tint = Color(0xFF333333)
          )
        }
        Surface(
          shape = CircleShape,
          color = Color.White,
          shadowElevation = 3.dp,
          modifier = Modifier
            .size(36.dp)
            .clickable { scale = (scale / 1.25f).coerceAtLeast(0.6f) }
        ) {
          Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "تصغير",
            modifier = Modifier.padding(8.dp),
            tint = Color(0xFF333333)
          )
        }
      }
    }

    // Bottom Selected Store Card Preview (When in Consumer / Nearby mode)
    if (selectedStore != null && !isPickerMode) {
      val store = selectedStore!!
      Card(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(12.dp)
          .testTag("map_store_details_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = store.bannerEmoji,
                fontSize = 24.sp,
                modifier = Modifier
                  .background(Color(0xFFF3F4F6), CircleShape)
                  .padding(8.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = store.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = Color(0xFF1E293B)
                )
                Text(
                  text = store.location?.districtName ?: "خدمة متنقلة",
                  fontSize = 12.sp,
                  color = Color(0xFF64748B)
                )
              }
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFFFFF8E1)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = StarGold,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "${store.rating} (${store.ratingsCount})",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFB45309)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = store.description,
            fontSize = 12.sp,
            color = Color(0xFF475569),
            maxLines = 2
          )

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "📍 المسافة التقديرية: 1.4 كم",
              fontSize = 12.sp,
              color = DeliveryBlue,
              fontWeight = FontWeight.Medium
            )
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = AmberPrimary,
              modifier = Modifier
                .clickable { onStoreSelected?.invoke(store) }
                .testTag("select_store_btn")
            ) {
              Text(
                text = "استعراض المنتجات والطلب",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
    }
  }
}
