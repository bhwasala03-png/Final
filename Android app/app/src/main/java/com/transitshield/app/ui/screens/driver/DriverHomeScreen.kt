package com.transitshield.app.ui.screens.driver

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.integration.android.ScanContract
import com.google.zxing.integration.android.ScanOptions
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.*
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.*
import com.transitshield.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.OutputStream

@Composable
fun DriverHomeScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    var dashboard by remember { mutableStateOf<DriverDashboardDto?>(null) }
    var schedule by remember { mutableStateOf<DriverScheduleDto?>(null) }
    var alerts by remember { mutableStateOf<List<DriverAlertDto>>(emptyList()) }
    var assignedQr by remember { mutableStateOf<BusQrCodeDto?>(null) }
    var validationResult by remember { mutableStateOf<TicketValidationResponse?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }

    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val scannedValue = result.contents
        if (!scannedValue.isNullOrBlank()) {
            scope.launch {
                runCatching {
                    RetrofitClient.apiService.validatePassengerTicket(TicketValidationRequest(scannedValue))
                }.onSuccess {
                    validationResult = it
                    selectedTab = 2
                }.onFailure {
                    infoMessage = it.message ?: "Ticket validation failed"
                }
            }
        }
    }

    fun refreshAll() {
        scope.launch {
            runCatching { RetrofitClient.apiService.getDriverDashboard() }.onSuccess { dashboard = it }
            runCatching { RetrofitClient.apiService.getDriverSchedule() }.onSuccess { schedule = it }
            runCatching { RetrofitClient.apiService.getDriverAlerts() }.onSuccess { alerts = it }
            runCatching { RetrofitClient.apiService.getAssignedBusQr() }.onSuccess { assignedQr = it }
        }
    }

    LaunchedEffect(Unit) { refreshAll() }

    val qrBitmap = remember(assignedQr?.qrToken) { assignedQr?.qrToken?.let { generateQrBitmap(it) } }

    Scaffold(
        containerColor = BgDeep,
        bottomBar = {
            DriverBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                navController = navController,
                alertCount = alerts.size
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgDeep)
        ) {
            DriverHeader(dashboard)

            infoMessage?.let {
                Text(
                    text = it,
                    color = OrangeWarning,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            when (selectedTab) {
                0 -> DriverDashboardTab(
                    dashboard = dashboard,
                    onViewAssignedQr = { showQrDialog = true },
                    onScanTicket = {
                        val options = ScanOptions().apply {
                            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            setPrompt("Scan passenger ticket QR")
                            setBeepEnabled(true)
                            setOrientationLocked(false)
                        }
                        scannerLauncher.launch(options)
                    },
                    onReportLostItem = { navController.navigate(Screen.LostItemReport.route) }
                )
                1 -> DriverScheduleTab(schedule)
                2 -> DriverAlertsTab(alerts, validationResult)
            }
        }
    }

    if (showQrDialog) {
        AssignedQrDialog(
            qr = assignedQr,
            qrBitmap = qrBitmap,
            onDismiss = { showQrDialog = false },
            onSave = {
                qrBitmap?.let {
                    val uri = saveQrToGallery(context, it, assignedQr?.qrLabel ?: "assigned_bus_qr")
                    if (uri != null) infoMessage = "QR saved to device gallery"
                }
            },
            onShare = {
                qrBitmap?.let {
                    val uri = saveQrToGallery(context, it, assignedQr?.qrLabel ?: "assigned_bus_qr")
                    if (uri != null) {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(context, Intent.createChooser(sendIntent, "Share assigned bus QR"), null)
                    }
                }
            }
        )
    }
}

@Composable
private fun DriverHeader(dashboard: DriverDashboardDto?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(BgSurface, BgDeep)))
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(BlueElectric, BlueDark))),
                contentAlignment = Alignment.Center
            ) {
                Text(dashboard?.profileInitial ?: "DR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(dashboard?.name ?: "Driver", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(dashboard?.depot ?: "Depot", color = TextSecondary, fontSize = 12.sp)
            }
            StatusBadge(if (dashboard?.isOnline == true) "ONLINE" else "OFFLINE")
        }
    }
}

@Composable
private fun DriverDashboardTab(
    dashboard: DriverDashboardDto?,
    onViewAssignedQr: () -> Unit,
    onScanTicket: () -> Unit,
    onReportLostItem: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BgCard)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Current Route", color = TextSecondary, fontSize = 12.sp)
                    Text(dashboard?.currentRoute ?: "No active route", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = onViewAssignedQr, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.QrCode, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Assigned Bus QR")
                        }
                        Button(onClick = onScanTicket, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)) {
                            Icon(Icons.Default.QrCodeScanner, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Scan Ticket")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onReportLostItem, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.FindInPage, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Report Lost Item")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardStatCard("Trips", (dashboard?.tripsToday ?: 0).toString(), Icons.Default.Route, modifier = Modifier.weight(1f))
                DashboardStatCard("On-Time", "${dashboard?.onTimePercentage ?: 0}%", Icons.Default.Timer, accentColor = GreenSuccess, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DriverScheduleTab(schedule: DriverScheduleDto?) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BgCard)) {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader("Today Schedule")
                    InfoRow("Assignment", schedule?.assignmentStatus ?: "-")
                    InfoRow("Route", "${schedule?.routeNumber ?: "-"} ${schedule?.routeName ?: ""}".trim())
                    InfoRow("Direction", "${schedule?.originName ?: "-"} → ${schedule?.destinationName ?: "-"}")
                    InfoRow("Bus", "${schedule?.busCode ?: "-"} ${schedule?.registrationNumber ?: ""}".trim())
                    InfoRow("Started", schedule?.startedAt ?: "-")
                    InfoRow("Active QR", if (schedule?.hasActiveQr == true) (schedule.activeQrLabel ?: "Available") else "Not Generated")
                }
            }
        }
    }
}

@Composable
private fun DriverAlertsTab(alerts: List<DriverAlertDto>, validationResult: TicketValidationResponse?) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)) {
        validationResult?.let { result ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (result.valid == true) GreenSuccess.copy(alpha = 0.15f) else RedError.copy(alpha = 0.15f))
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(if (result.valid == true) "Ticket Valid" else "Ticket Invalid", fontWeight = FontWeight.Bold)
                        Text(result.message ?: "", fontSize = 12.sp)
                        Text("Trip: ${result.tripRef ?: "-"} • Passenger: ${result.passengerName ?: "-"}", fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        if (alerts.isEmpty()) {
            item { Text("No driver alerts.", color = TextMuted) }
        }
        items(alerts) { alert ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BgCard)) {
                Column(Modifier.padding(14.dp)) {
                    Text(alert.title ?: "Alert", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(alert.message ?: "", color = TextSecondary, fontSize = 12.sp)
                    Text(alert.timestamp ?: "", color = TextMuted, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun AssignedQrDialog(
    qr: BusQrCodeDto?,
    qrBitmap: Bitmap?,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assigned Bus QR") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (qrBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = androidx.compose.ui.graphics.asImageBitmap(qrBitmap),
                        contentDescription = "Assigned bus QR",
                        modifier = Modifier.size(220.dp)
                    )
                }
                Text(qr?.qrLabel ?: "No active QR assigned", fontSize = 12.sp, color = TextSecondary)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onSave) { Text("Save") }
                TextButton(onClick = onShare) { Text("Share") }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}

private fun generateQrBitmap(content: String, size: Int = 900): Bitmap {
    val bits = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp.setPixel(x, y, if (bits[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
        }
    }
    return bmp
}

private fun saveQrToGallery(context: android.content.Context, bitmap: Bitmap, filenameBase: String): Uri? {
    val resolver = context.contentResolver
    val filename = "${filenameBase.replace(" ", "_")}_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TransitShield")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
    var stream: OutputStream? = null
    return try {
        stream = resolver.openOutputStream(uri)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        uri
    } catch (_: Exception) {
        null
    } finally {
        stream?.close()
    }
}

@Composable
private fun DriverBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    navController: NavController,
    alertCount: Int
) {
    NavigationBar(containerColor = BgSurface, tonalElevation = 0.dp) {
        val items = listOf(
            Triple("Dashboard", Icons.Default.Dashboard, Screen.DriverHome.route),
            Triple("Schedule", Icons.Default.EventNote, Screen.DriverHome.route),
            Triple("Alerts", Icons.Default.NotificationsActive, Screen.DriverHome.route),
            Triple("Profile", Icons.Default.Person, Screen.DriverProfile.route)
        )
        items.forEachIndexed { index, (label, icon, route) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = {
                    onTabSelected(index)
                    if (index == 3) navController.navigate(route)
                },
                icon = {
                    BadgedBox(badge = { if (index == 2 && alertCount > 0) Badge { Text(alertCount.toString()) } }) {
                        Icon(icon, contentDescription = label)
                    }
                },
                label = { Text(label, fontSize = 11.sp) }
            )
        }
    }
}
