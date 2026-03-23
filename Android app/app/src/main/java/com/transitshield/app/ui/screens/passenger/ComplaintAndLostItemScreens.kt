package com.transitshield.app.ui.screens.passenger

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.ui.components.*
import com.transitshield.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ComplaintSubmissionScreen(navController: NavController) {
    var busRoute by remember { mutableStateOf("") }
    var incidentType by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    if (submitted) {
        SubmittedSuccessCard(title = "Complaint Submitted", message = "Your complaint has been reported. We will review it shortly.") {
            navController.popBackStack()
        }
        return
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
                        Spacer(Modifier.width(8.dp))
                        Text("Incident Report", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Help us improve transit safety by reporting incidents.", color = TextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            ComplaintFormField(
                label = "Bus Route / Plate Number",
                value = busRoute,
                onValueChange = { busRoute = it },
                placeholder = "e.g. Route 138 or NC-3421"
            )

            Spacer(Modifier.height(14.dp))

            // Incident Type Dropdown
            Column {
                Text("Incident Type", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
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
                                modifier = Modifier.clickable { dropdownExpanded = true }
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
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        listOf("Reckless Driving", "Rude Behavior", "Overcharging", "Bus Cleanliness", "Late Departure", "Other").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, color = TextPrimary) },
                                onClick = { incidentType = type; dropdownExpanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            ComplaintFormField(
                label = "Additional Details",
                value = details,
                onValueChange = { details = it },
                placeholder = "Describe the incident in detail...",
                singleLine = false,
                maxLines = 5
            )

            Spacer(Modifier.height(14.dp))

            // Evidence Upload Placeholder
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
                    Spacer(Modifier.height(6.dp))
                    Text("Upload Evidence (Optional)", color = TextSecondary, fontSize = 13.sp)
                    Text("Photos or video up to 10MB", color = TextMuted, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = "Submit Complaint",
                onClick = { submitted = true },
                enabled = busRoute.isNotBlank() && incidentType.isNotBlank()
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun LostItemReportScreen(navController: NavController) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var itemTitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var routeInfo by remember { mutableStateOf("") }
    var busInfo by remember { mutableStateOf("") }
    var lostAt by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contactDetails by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var myReports by remember { mutableStateOf(listOf<com.transitshield.app.data.network.dto.LostItemReportDto>()) }

    suspend fun loadMyReports() {
        runCatching { com.transitshield.app.data.network.RetrofitClient.apiService.getMyLostItemReports() }
            .onSuccess { myReports = it }
    }

    LaunchedEffect(Unit) { loadMyReports() }

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
                        Spacer(Modifier.width(8.dp))
                        Text("Lost Item Report", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Report a lost item and track its status.", color = TextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            ComplaintFormField("Item Title", itemTitle, { itemTitle = it }, placeholder = "e.g. Black backpack")
            Spacer(Modifier.height(10.dp))
            ComplaintFormField("Category", category, { category = it }, placeholder = "e.g. Bag / Phone / Document")
            Spacer(Modifier.height(10.dp))
            ComplaintFormField("Route", routeInfo, { routeInfo = it }, placeholder = "e.g. Route 138")
            Spacer(Modifier.height(10.dp))
            ComplaintFormField("Bus", busInfo, { busInfo = it }, placeholder = "e.g. NC-3421")
            Spacer(Modifier.height(10.dp))
            ComplaintFormField("Lost Date/Time (ISO)", lostAt, { lostAt = it }, placeholder = "e.g. 2026-03-23T11:30:00")
            Spacer(Modifier.height(10.dp))
            ComplaintFormField(
                "Item Description",
                description,
                { description = it },
                placeholder = "Describe the item...",
                singleLine = false,
                maxLines = 4
            )
            Spacer(Modifier.height(10.dp))
            ComplaintFormField("Contact Details", contactDetails, { contactDetails = it }, placeholder = "Phone or email")

            errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = RedError, fontSize = 12.sp)
            }

            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = "Submit Lost Item Report",
                onClick = {
                    errorMessage = null
                    coroutineScope.launch {
                        runCatching {
                            com.transitshield.app.data.network.RetrofitClient.apiService.createLostItemReport(
                                com.transitshield.app.data.network.dto.LostItemReportCreateRequest(
                                    itemTitle = itemTitle,
                                    description = description,
                                    category = category.takeIf { it.isNotBlank() },
                                    routeInfo = routeInfo.takeIf { it.isNotBlank() },
                                    busInfo = busInfo.takeIf { it.isNotBlank() },
                                    lostAt = lostAt.takeIf { it.isNotBlank() },
                                    contactDetails = contactDetails.takeIf { it.isNotBlank() }
                                )
                            )
                        }.onSuccess {
                            submitted = true
                            itemTitle = ""; category = ""; routeInfo = ""; busInfo = ""; lostAt = ""; description = ""; contactDetails = ""
                            loadMyReports()
                        }.onFailure {
                            errorMessage = it.message ?: "Failed to submit report"
                        }
                    }
                },
                enabled = itemTitle.isNotBlank() && description.isNotBlank()
            )

            Spacer(Modifier.height(20.dp))
            SectionHeader("My Lost Item Reports")
            Spacer(Modifier.height(8.dp))
            if (myReports.isEmpty()) {
                Text("No reports submitted yet.", color = TextMuted, fontSize = 13.sp)
            } else {
                myReports.forEach { report ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
            Spacer(Modifier.height(16.dp))
        }
    }
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
                Spacer(Modifier.height(16.dp))
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(8.dp))
                Text(message, color = TextSecondary, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(24.dp))
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
