package com.transitshield.app.ui.screens.passenger

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.components.PrimaryButton
import com.transitshield.app.ui.theme.*

@Composable
fun QrScanScreen(navController: NavController) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    Scaffold(
        topBar = { AppTopBar(title = "Scan QR Code", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Point camera at the QR code inside the bus",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Fake Scanner Frame
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BgCard)
                    .border(2.dp, BlueElectric.copy(alpha = borderAlpha), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                // Corner decorations
                ScannerCorners()

                // Scan Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, BlueElectric, Color.Transparent)
                            )
                        )
                        .offset(y = (260 * scanLineY).dp)
                )

                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = BlueElectric.copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("The QR code inside the bus identifies:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    listOf("🚌 Bus ID & vehicle info", "🗺 Route & direction", "👤 Current driver", "📍 All route stops").forEach { item ->
                        Text(text = item, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            PrimaryButton(
                text = "Simulate Successful Scan",
                onClick = { navController.navigate(Screen.TripDetails.route) }
            )

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Cancel", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun ScannerCorners() {
    val cornerSize = 24.dp
    val cornerThickness = 3.dp
    val cornerColor = BlueElectric

    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left
        Row(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
            Box(modifier = Modifier.size(cornerThickness, cornerSize).background(cornerColor))
            Box(modifier = Modifier.size(cornerSize - cornerThickness, cornerThickness).background(cornerColor))
        }
        // Top-right
        Row(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
            Box(modifier = Modifier.size(cornerSize - cornerThickness, cornerThickness).background(cornerColor))
            Box(modifier = Modifier.size(cornerThickness, cornerSize).background(cornerColor))
        }
        // Bottom-left
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Box(modifier = Modifier.size(cornerSize, cornerThickness).background(cornerColor))
            Box(modifier = Modifier.size(cornerThickness, cornerSize - cornerThickness).background(cornerColor))
        }
        // Bottom-right
        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp), horizontalAlignment = Alignment.End) {
            Box(modifier = Modifier.size(cornerSize, cornerThickness).background(cornerColor))
            Box(modifier = Modifier.size(cornerThickness, cornerSize - cornerThickness).background(cornerColor))
        }
    }
}
