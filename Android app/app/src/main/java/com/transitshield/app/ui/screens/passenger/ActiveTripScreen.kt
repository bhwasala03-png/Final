package com.transitshield.app.ui.screens.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.*
import com.transitshield.app.ui.theme.*

@Composable
fun ActiveTripScreen(navController: NavController) {
    var showExtendDialog by remember { mutableStateOf(false) }

    if (showExtendDialog) {
        ExtendTripDialog(
            onDismiss = { showExtendDialog = false },
            onConfirm = { showExtendDialog = false }
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Active Trip", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Status Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(BlueElectric.copy(alpha = 0.3f), BlueDark.copy(alpha = 0.2f))))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(GreenSuccess)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("TRIP IN PROGRESS", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Route 138", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                    Text("Pettah to Maharagama", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Trip Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader("Trip Information")
                    Spacer(Modifier.height(4.dp))
                    InfoRow("Trip Reference", "REF-8A2F1C")
                    InfoRow("Bus ID", "NC-3421")
                    InfoRow("Driver", "Sunil Perera")

                    Spacer(Modifier.height(12.dp))

                    // Journey Visualization
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(12.dp).clip(androidx.compose.foundation.shape.CircleShape).background(GreenSuccess)
                            )
                            Box(modifier = Modifier.width(2.dp).height(30.dp).background(TextMuted))
                            Box(
                                modifier = Modifier.size(12.dp).clip(androidx.compose.foundation.shape.CircleShape).background(BlueElectric)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Narahenpita", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Spacer(Modifier.height(18.dp))
                            Text("Nugegoda", color = BlueElectric, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Boarding", color = TextMuted, fontSize = 11.sp)
                            Spacer(Modifier.height(18.dp))
                            Text("Destination", color = BlueLight, fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Divider(color = BorderSubtle)
                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Fare Paid", color = TextMuted, fontSize = 11.sp)
                            Text("LKR 45.00", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        StatusBadge("ACTIVE")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Notice Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeWarning.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangeWarning.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = OrangeWarning, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Travelling beyond your selected stop? Use 'Extend Trip' to update your destination.",
                        color = OrangeWarning,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = "Show Ticket to Conductor",
                onClick = { navController.navigate(Screen.ConductorVerification.route) }
            )

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showExtendDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeWarning)
                ) {
                    Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Extend Trip", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ExtendTripDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val stops = listOf("Nugegoda", "Maharagama", "Kottawa")
    var selectedStop by remember { mutableStateOf(stops.firstOrNull() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Extend Trip", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Select your new destination", color = TextSecondary, fontSize = 13.sp)

                Spacer(Modifier.height(16.dp))

                stops.forEach { stop ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStop == stop,
                            onClick = { selectedStop = stop },
                            colors = RadioButtonDefaults.colors(selectedColor = BlueElectric)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stop, color = TextPrimary, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BlueElectric.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Additional Fare", color = TextSecondary, fontSize = 13.sp)
                        Text("LKR 20.00", color = BlueElectric, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueElectric)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
