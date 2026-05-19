package de.westnordost.streetcomplete.screens.measure

actual class ArSupportChecker {
    // the AR measure app (StreetMeasure) does not exist on iOS, so, measuring with AR
    // is not supported
    actual operator fun invoke(): Boolean = false
}
