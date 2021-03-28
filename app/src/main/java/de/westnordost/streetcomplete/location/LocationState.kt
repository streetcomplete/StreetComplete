package de.westnordost.streetcomplete.location

enum class LocationState {
    DENIED,  // user declined to give this app access to location
    ALLOWED,  // user allowed this app to access location (but location disabled)
    ENABLED,  // location service is turned on (but no location request active)
    SEARCHING,  // requested location updates and waiting for first fix
    UPDATING;

    // receiving location updates
    val isEnabled: Boolean get() = ordinal >= ENABLED.ordinal
}