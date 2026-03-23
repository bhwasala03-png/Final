package com.transitshield.app.ui.screens.conductor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.ui.components.*
import com.transitshield.app.ui.theme.*

@Composable
fun ConductorVerificationScreen(navController: NavController) {
    var isChecked by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(title = "Ticket Verification", onBack = { navController.popBackStack() })
        },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Verification Status Banner
            if (isChecked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GreenSuccess.copy(alpha = 0.12f))
                        .border(1.dp, GreenSuccess.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Ticket Verified ✓", color = GreenSuccess, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text("Marked as checked by conductor", color = GreenSuccess.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Passenger Info Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(BlueElectric, BlueDark))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("RP", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Rahul Perera", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Passenger ID: PASS-7429", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        StatusBadge("VERIFIED PASSENGER")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Ticket Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(WalletGrad1, WalletGrad2)))
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CONDUCT VERIFICATION", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, letterSpacing = 1.sp)
                                Text("Route 138", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                            }
                            StatusBadge("PAID")
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow("Trip Reference", "REF-8A2F1C")
                        InfoRow("Passenger", "Rahul Perera")
                        InfoRow("Bus ID", "NC-3421")
                        InfoRow("Route", "138 – Pettah to Maharagama")
                        InfoRow("Boarding Stop", "Narahenpita")
                        InfoRow("Destination", "Nugegoda")
                        InfoRow("Fare Paid", "LKR 45.00")
                        InfoRow("Payment Status", "Success")
                        InfoRow("Date & Time", "Today, 08:32 AM")
                        InfoRow("Receipt Status", "Issued")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // QR Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = BlueElectric, modifier = Modifier.size(70.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Ticket QR", color = TextSecondary, fontSize = 12.sp)
                        Text("REF-8A2F1C", color = BlueElectric, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        if (isChecked) {
                            Text("✓ Scanned & Verified", color = GreenSuccess, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        } else {
                            Text("Awaiting verification", color = OrangeWarning, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (!isChecked) {
                Button(
                    onClick = { isChecked = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Mark as Checked", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                }
                Spacer(Modifier.height(10.dp))
                SecondaryButton(
                    text = "Report Issue",
                    onClick = {}
                )
            } else {
                PrimaryButton(
                    text = "Done – Back to Ticket",
                    onClick = { navController.popBackStack() }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
