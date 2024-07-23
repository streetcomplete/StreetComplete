package de.westnordost.streetcomplete.screens.main.controls

/** State of location updates */
enum class LocationState {
    /** user declined to give this app access to location */
    DENIED,
    /** user allowed this app to access location (but location disabled) */
    ALLOWED,
    /** location service is turned on (but no location request active) */
    ENABLED,
    /** requested location updates and waiting for first fix */
    SEARCHING,
    /** receiving location updates */
    UPDATING;

    val isEnabled: Boolean get() = ordinal >= ENABLED.ordinal
}
