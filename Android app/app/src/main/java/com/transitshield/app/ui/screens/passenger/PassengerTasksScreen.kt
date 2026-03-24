package com.transitshield.app.ui.screens.passenger

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.PassengerTaskCapacityRequest
import com.transitshield.app.data.network.dto.PassengerTaskLocationRequest
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.BlueElectric
import com.transitshield.app.ui.theme.BorderSubtle
import com.transitshield.app.ui.theme.TextMuted
import com.transitshield.app.ui.theme.TextPrimary
import com.transitshield.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val PREFS_NAME = "passenger_tasks_rate_limit"
private const val KEY_DAY = "task_day"
private const val KEY_DAILY_COUNT = "daily_click_count"
private const val DAILY_LIMIT = 6

@Composable
fun PassengerTasksScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var showCapacityDialog by remember { mutableStateOf(false) }
    var capacityInput by remember { mutableStateOf("") }
    var activeTripId by remember { mutableStateOf<Long?>(null) }
    var dailyCount by remember { mutableStateOf(0) }
    var usedForActiveTrip by remember { mutableStateOf(false) }

    fun syncDayCounter() {
        val today = LocalDate.now().toString()
        val savedDay = prefs.getString(KEY_DAY, null)
        if (savedDay != today) {
            prefs.edit().putString(KEY_DAY, today).putInt(KEY_DAILY_COUNT, 0).apply()
        }
        dailyCount = prefs.getInt(KEY_DAILY_COUNT, 0)
    }

    fun isTripLimited(tripId: Long?): Boolean {
        return tripId != null && prefs.getBoolean("trip_used_$tripId", false)
    }

    fun markTaskUsage() {
        val nextCount = (dailyCount + 1).coerceAtMost(DAILY_LIMIT)
        val editor = prefs.edit().putInt(KEY_DAILY_COUNT, nextCount)
        activeTripId?.let { editor.putBoolean("trip_used_$it", true) }
        editor.apply()
        dailyCount = nextCount
        usedForActiveTrip = isTripLimited(activeTripId)
    }

    LaunchedEffect(Unit) {
        syncDayCounter()
        activeTripId = runCatching { RetrofitClient.apiService.getMyActiveTrip().id }.getOrNull()
        usedForActiveTrip = isTripLimited(activeTripId)
    }

    val reachedDailyLimit = dailyCount >= DAILY_LIMIT
    val reachedTripLimit = usedForActiveTrip
    val canSubmitTask = !reachedDailyLimit && !reachedTripLimit

    Scaffold(
        topBar = { AppTopBar(title = "Passenger Tasks", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Daily submissions: $dailyCount / $DAILY_LIMIT",
                color = if (reachedDailyLimit) TextMuted else TextSecondary,
                fontSize = 12.sp
            )
            if (reachedTripLimit) {
                Text(
                    text = "You already submitted a task for your current active trip.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            TaskCard(
                title = "Update Passenger Count (+2 Pts)",
                description = "Submit current headcount for your bus.",
                icon = { Icon(Icons.Default.Groups, contentDescription = null, tint = BlueElectric) },
                enabled = canSubmitTask,
                onClick = {
                    if (!canSubmitTask) return@TaskCard
                    showCapacityDialog = true
                }
            )

            TaskCard(
                title = "Share Live Location (+5 Pts)",
                description = "Share your current bus location.",
                icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BlueElectric) },
                enabled = canSubmitTask,
                onClick = {
                    if (!canSubmitTask) return@TaskCard
                    scope.launch {
                        runCatching {
                            RetrofitClient.apiService.submitTaskLocation(
                                PassengerTaskLocationRequest(
                                    busId = 1L,
                                    latitude = 6.9271,
                                    longitude = 79.8612
                                )
                            )
                        }.onSuccess {
                            markTaskUsage()
                            Toast.makeText(context, it.message ?: "Location shared", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            Toast.makeText(context, it.message ?: "Failed to share location", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }

    if (showCapacityDialog) {
        AlertDialog(
            onDismissRequest = { showCapacityDialog = false },
            title = { Text("Submit Capacity") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the estimated passenger headcount.", color = TextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = capacityInput,
                        onValueChange = { value ->
                            capacityInput = value.filter { it.isDigit() }
                        },
                        label = { Text("Headcount") },
                        placeholder = { Text("e.g. 34") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = capacityInput.toIntOrNull()
                        if (count == null || count <= 0) {
                            Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            runCatching {
                                RetrofitClient.apiService.submitTaskCapacity(
                                    PassengerTaskCapacityRequest(
                                        busId = 1L,
                                        passengerCount = count
                                    )
                                )
                            }.onSuccess {
                                markTaskUsage()
                                showCapacityDialog = false
                                capacityInput = ""
                                Toast.makeText(context, it.message ?: "Capacity submitted", Toast.LENGTH_SHORT).show()
                            }.onFailure {
                                Toast.makeText(context, it.message ?: "Failed to submit capacity", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = canSubmitTask
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCapacityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TaskCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        enabled = enabled
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            icon()
            Spacer(Modifier.height(10.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            Text(description, color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                if (enabled) "Tap to submit" else "Limit reached",
                color = if (enabled) BlueElectric else TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
