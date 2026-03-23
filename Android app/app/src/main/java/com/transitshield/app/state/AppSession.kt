package com.transitshield.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.transitshield.app.data.network.dto.BusAssignmentDto
import com.transitshield.app.data.network.dto.PassengerTripDto
import com.transitshield.app.data.network.dto.RouteVariantStopDto
import com.transitshield.app.data.network.dto.UserDto

object AppSession {
    var currentUser by mutableStateOf<UserDto?>(null)
    var activeAssignments by mutableStateOf<List<BusAssignmentDto>>(emptyList())
    var selectedAssignment by mutableStateOf<BusAssignmentDto?>(null)

    var selectedBoardingStop by mutableStateOf<RouteVariantStopDto?>(null)
    var selectedDestinationStop by mutableStateOf<RouteVariantStopDto?>(null)

    var farePreviewLkr by mutableStateOf<Double?>(null)
    var activeTrip by mutableStateOf<PassengerTripDto?>(null)

    fun clearRidePlanning() {
        selectedAssignment = null
        selectedBoardingStop = null
        selectedDestinationStop = null
        farePreviewLkr = null
    }

    fun clearTripState() {
        activeTrip = null
        clearRidePlanning()
    }

    fun clearAll() {
        currentUser = null
        activeAssignments = emptyList()
        clearTripState()
    }
}
