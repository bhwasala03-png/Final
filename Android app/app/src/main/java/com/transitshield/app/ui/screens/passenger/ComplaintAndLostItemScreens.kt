package com.transitshield.app.ui.screens.passenger

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.ComplaintRequest
import com.transitshield.app.data.network.dto.LostItemReportCreateRequest
import com.transitshield.app.data.network.dto.LostItemReportDto
import com.transitshield.app.data.network.dto.PassengerTripDto
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.components.ComplaintFormField
import com.transitshield.app.ui.components.PrimaryButton
import com.transitshield.app.ui.components.SectionHeader
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.BlueElectric
import com.transitshield.app.ui.theme.BorderSubtle
import com.transitshield.app.ui.theme.GreenSuccess
import com.transitshield.app.ui.theme.PurpleInfo
import com.transitshield.app.ui.theme.RedError
import com.transitshield.app.ui.theme.TextMuted
import com.transitshield.app.ui.theme.TextPrimary
import com.transitshield.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ComplaintSubmissionScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var incidentType by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var incidentDropdownExpanded by remember { mutableStateOf(false) }
    var tripDropdownExpanded by remember { mutableStateOf(false) }
    var tripHistory by remember { mutableStateOf<List<PassengerTripDto>>(emptyList()) }
    var selectedTrip by remember { mutableStateOf<PassengerTripDto?>(null) }

    LaunchedEffect(Unit) {
        tripHistory = runCatching { RetrofitClient.apiService.getMyTripHistory() }.getOrDefault(emptyList())
    }

    Scaffold(
        topBar = { AppTopBar(title = "Submit Complaint", onBack = { navController.popBackStack() }) },
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Report, contentDescription = null, tint = RedError, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("Incident Report", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text("Help us improve transit safety by reporting incidents.", color = TextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            TripHistoryDropdown(
                label = "Select Past Trip",
                tripHistory = tripHistory,
                selectedTrip = selectedTrip,
                expanded = tripDropdownExpanded,
                onExpandedChange = { tripDropdownExpanded = it },
                onTripSelected = { selectedTrip = it }
            )

            Spacer(Modifier.height(8.dp))

            Column {
                Text("Incident Type", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(3.dp))
                Box {
                    OutlinedTextField(
                        value = incidentType,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select incident type", color = TextMuted, fontSize = 14.sp) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = BlueElectric,
                                modifier = Modifier.clickable { incidentDropdownExpanded = true }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueElectric,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgCard,
                            unfocusedContainerColor = BgCard
                        )
                    )
                    DropdownMenu(
                        expanded = incidentDropdownExpanded,
                        onDismissRequest = { incidentDropdownExpanded = false }
                    ) {
                        listOf("Reckless Driving", "Rude Behavior", "Overcharging", "Bus Cleanliness", "Late Departure", "Other").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, color = TextPrimary) },
                                onClick = { incidentType = type; incidentDropdownExpanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            ComplaintFormField(
                label = "Additional Details",
                value = details,
                onValueChange = { details = it },
                placeholder = "Describe the incident in detail...",
                singleLine = false,
                maxLines = 5
            )

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = BlueElectric, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(3.dp))
                    Text("Upload Evidence (Optional)", color = TextSecondary, fontSize = 13.sp)
                    Text("Photos or video up to 10MB", color = TextMuted, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            PrimaryButton(
                text = "Submit Complaint",
                onClick = {
                    val tripId = selectedTrip?.id ?: return@PrimaryButton
                    coroutineScope.launch {
                        runCatching {
                            RetrofitClient.apiService.submitComplaint(
                                ComplaintRequest(
                                    tripId = tripId,
                                    description = details,
                                    incidentType = incidentType
                                )
                            )
                        }.onSuccess {
                            Toast.makeText(context, it.message ?: "Complaint submitted", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }.onFailure {
                            Toast.makeText(context, it.message ?: "Failed to submit complaint", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = selectedTrip != null && incidentType.isNotBlank() && details.isNotBlank()
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun LostItemReportScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var itemTitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var lostAt by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contactDetails by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var myReports by remember { mutableStateOf(listOf<LostItemReportDto>()) }
    var tripHistory by remember { mutableStateOf<List<PassengerTripDto>>(emptyList()) }
    var selectedTrip by remember { mutableStateOf<PassengerTripDto?>(null) }
    var tripDropdownExpanded by remember { mutableStateOf(false) }

    suspend fun loadMyReports() {
        runCatching { RetrofitClient.apiService.getMyLostItemReports() }
            .onSuccess { myReports = it }
    }

    suspend fun loadTripHistory() {
        runCatching { RetrofitClient.apiService.getMyTripHistory() }
            .onSuccess { tripHistory = it }
    }

    LaunchedEffect(Unit) {
        loadMyReports()
        loadTripHistory()
    }

    if (submitted) {
        SubmittedSuccessCard(title = "Report Submitted", message = "Your lost item report has been filed and saved.") {
            submitted = false
            errorMessage = null
            navController.popBackStack()
        }
        return
    }

    Scaffold(
        topBar = { AppTopBar(title = "Report Lost Item", onBack = { navController.popBackStack() }) },
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FindInPage, contentDescription = null, tint = PurpleInfo, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("Lost Item Report", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text("Report a lost item and track its status.", color = TextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            ComplaintFormField("Item Title", itemTitle, { itemTitle = it }, placeholder = "e.g. Black backpack")
            Spacer(Modifier.height(5.dp))
            ComplaintFormField("Category", category, { category = it }, placeholder = "e.g. Bag / Phone / Document")
            Spacer(Modifier.height(5.dp))

            TripHistoryDropdown(
                label = "Select Past Trip",
                tripHistory = tripHistory,
                selectedTrip = selectedTrip,
                expanded = tripDropdownExpanded,
                onExpandedChange = { tripDropdownExpanded = it },
                onTripSelected = { selectedTrip = it }
            )

            Spacer(Modifier.height(5.dp))
            ComplaintFormField("Lost Date/Time (ISO)", lostAt, { lostAt = it }, placeholder = "e.g. 2026-03-23T11:30:00")
            Spacer(Modifier.height(5.dp))
            ComplaintFormField(
                "Item Description",
                description,
                { description = it },
                placeholder = "Describe the item...",
                singleLine = false,
                maxLines = 4
            )
            Spacer(Modifier.height(5.dp))
            ComplaintFormField("Contact Details", contactDetails, { contactDetails = it }, placeholder = "Phone or email")

            errorMessage?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = RedError, fontSize = 12.sp)
            }

            Spacer(Modifier.height(9.dp))
            PrimaryButton(
                text = "Submit Lost Item Report",
                onClick = {
                    errorMessage = null
                    coroutineScope.launch {
                        runCatching {
                            RetrofitClient.apiService.createLostItemReport(
                                LostItemReportCreateRequest(
                                    tripId = selectedTrip?.id,
                                    busId = selectedTrip?.busId,
                                    itemTitle = itemTitle,
                                    description = description,
                                    category = category.takeIf { it.isNotBlank() },
                                    routeInfo = selectedTrip?.routeNumber,
                                    busInfo = selectedTrip?.busCode,
                                    lostAt = lostAt.takeIf { it.isNotBlank() },
                                    contactDetails = contactDetails.takeIf { it.isNotBlank() }
                                )
                            )
                        }.onSuccess {
                            submitted = true
                            itemTitle = ""
                            category = ""
                            lostAt = ""
                            description = ""
                            contactDetails = ""
                            selectedTrip = null
                            loadMyReports()
                        }.onFailure {
                            errorMessage = it.message ?: "Failed to submit report"
                        }
                    }
                },
                enabled = itemTitle.isNotBlank() && description.isNotBlank() && selectedTrip != null
            )

            Spacer(Modifier.height(10.dp))
            SectionHeader("My Lost Item Reports")
            Spacer(Modifier.height(4.dp))
            if (myReports.isEmpty()) {
                Text("No reports submitted yet.", color = TextMuted, fontSize = 13.sp)
            } else {
                myReports.forEach { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(report.itemTitle ?: "-", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(report.description ?: "", color = TextSecondary, fontSize = 12.sp)
                            Text("Status: ${report.status ?: "REPORTED"}", color = BlueElectric, fontSize = 12.sp)
                            report.adminNotes?.takeIf { it.isNotBlank() }?.let { Text("Admin: $it", color = TextMuted, fontSize = 11.sp) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TripHistoryDropdown(
    label: String,
    tripHistory: List<PassengerTripDto>,
    selectedTrip: PassengerTripDto?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onTripSelected: (PassengerTripDto) -> Unit
) {
    Column {
        Text(label, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(3.dp))
        Box {
            OutlinedTextField(
                value = selectedTrip?.let { formatTripLabel(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Choose from trip history", color = TextMuted, fontSize = 14.sp) },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = BlueElectric,
                        modifier = Modifier.clickable { onExpandedChange(true) }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueElectric,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = BgCard,
                    unfocusedContainerColor = BgCard
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                tripHistory.forEach { trip ->
                    DropdownMenuItem(
                        text = { Text(formatTripLabel(trip), color = TextPrimary) },
                        onClick = {
                            onTripSelected(trip)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

private fun formatTripLabel(trip: PassengerTripDto): String {
    val route = trip.routeNumber ?: "Route N/A"
    val bus = trip.busCode ?: "Bus N/A"
    val ref = trip.tripRef ?: "Trip"
    return "$route • $bus • $ref"
}

@Composable
private fun SubmittedSuccessCard(title: String, message: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgCard)
        ) {
            Column(
                modifier = Modifier.padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(8.dp))
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(4.dp))
                Text(message, color = TextSecondary, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueElectric)
                ) {
                    Text("Back", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
