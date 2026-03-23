package com.transitshield.app.ui.screens.conductor

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.PassengerTripDto
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.components.InfoRow
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.TextMuted
import com.transitshield.app.ui.theme.TextPrimary
import com.transitshield.app.ui.theme.TextSecondary

@Composable
fun ConductorVerificationScreen(navController: NavController) {
    var trip by remember { mutableStateOf<PassengerTripDto?>(null) }

    LaunchedEffect(Unit) {
        runCatching {
            RetrofitClient.apiService.getMyActiveTrip()
        }.onSuccess { trip = it }
    }

    val payload = trip?.tripRef?.let { "TS_TRIP:$it" }
    val qrBitmap = remember(payload) { payload?.let { generateQrBitmap(it) } }

    Scaffold(
        topBar = { AppTopBar(title = "My Ticket QR", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BgCard)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.QrCode2, contentDescription = null)
                    Text("Show this QR to the driver scanner", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    if (qrBitmap != null) {
                        Image(bitmap = qrBitmap.asImageBitmap(), contentDescription = "Passenger ticket QR", modifier = Modifier.size(260.dp))
                    } else {
                        Text("No active paid trip found.", color = TextMuted)
                    }
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Trip Ref", trip?.tripRef ?: "-")
                    InfoRow("Status", trip?.tripStatus ?: "-")
                    InfoRow("Fare", "LKR ${trip?.totalFareLkr ?: 0.0}")
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("The QR encodes your active trip reference and is validated against driver assignment.", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

private fun generateQrBitmap(content: String, size: Int = 900): Bitmap {
    val bits = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) for (y in 0 until size) bmp.setPixel(x, y, if (bits[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
    return bmp
}
