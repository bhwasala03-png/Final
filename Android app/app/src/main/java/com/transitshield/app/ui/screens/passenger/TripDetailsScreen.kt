package com.transitshield.app.ui.screens.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.*
import com.transitshield.app.ui.theme.*

@Composable
fun TripDetailsScreen(navController: NavController) {
    val stops = listOf("Stop A", "Stop B", "Stop C", "Stop D", "Stop E", "Stop F", "Stop G")
    var selectedDestIndex by remember { mutableStateOf(5) } // Nugegoda default
    var dropdownExpanded by remember { mutableStateOf(false) }
    val boardingIndex = 3 // Narahenpita
    val baseFares = listOf(0, 15, 20, 30, 20, 45, 55, 70) // relative fare per stop from boarding
    val fare = if (selectedDestIndex > boardingIndex) baseFares[selectedDestIndex - boardingIndex] else 0

    Scaffold(
        topBar = { AppTopBar(title = "Trip Details", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Bus", color = TextMuted, fontSize = 11.sp)
                            Text("NC-3421", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Route", color = TextMuted, fontSize = 11.sp)
                            Text("138", color = BlueElectric, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Divider(color = BorderSubtle)
                    Spacer(Modifier.height(10.dp))
                    Text("Pettah - Maharagama", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = BlueElectric, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Driver: Sunil Perera", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionHeader("Your Journey")
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Boarding Stop
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(GreenSuccess)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Boarding Stop", color = TextMuted, fontSize = 11.sp)
                            Text(stops[boardingIndex], color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .width(2.dp)
                            .height(24.dp)
                            .background(BorderSubtle)
                    )

                    // Destination Dropdown
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(BlueElectric)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Destination Stop", color = TextMuted, fontSize = 11.sp)
                            Box {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BgElevated)
                                        .clickable { dropdownExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stops[selectedDestIndex], color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BlueElectric)
                                }
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    stops.drop(boardingIndex + 1).forEach { stop ->
                                        DropdownMenuItem(
                                            text = { Text(stop, color = TextPrimary) },
                                            onClick = {
                                                selectedDestIndex = stops.indexOf(stop)
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stop List Preview
            SectionHeader("Route Stops")
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    stops.forEachIndexed { index, stop ->
                        val isBoarding = index == boardingIndex
                        val isDestination = index == selectedDestIndex
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(
                                        when {
                                            isBoarding -> GreenSuccess
                                            isDestination -> BlueElectric
                                            else -> TextMuted
                                        }
                                    )
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stop,
                                color = when {
                                    isBoarding || isDestination -> TextPrimary
                                    else -> TextMuted
                                },
                                fontWeight = if (isBoarding || isDestination) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            if (isBoarding) {
                                Spacer(Modifier.width(6.dp))
                                StatusBadge("Boarding")
                            }
                            if (isDestination) {
                                Spacer(Modifier.width(6.dp))
                                StatusBadge("Destination")
                            }
                        }
                        if (index < stops.lastIndex) {
                            Divider(color = BorderSubtle.copy(alpha = 0.4f), modifier = Modifier.padding(start = 18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Fare Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader("Fare Summary")
                    Spacer(Modifier.height(4.dp))
                    InfoRow("Distance Stops", "${selectedDestIndex - boardingIndex} stops")
                    InfoRow("Base Fare", "LKR ${fare}.00")
                    InfoRow("Service Charge", "LKR 0.00")
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Fare", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("LKR ${fare}.00", color = BlueElectric, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Wallet Balance
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GreenSuccess.copy(alpha = 0.10f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Wallet Balance: ", color = TextSecondary, fontSize = 13.sp)
                    Text("LKR 1,245.50", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = "Confirm Journey & Pay",
                onClick = { navController.navigate(Screen.Payment.route) }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
