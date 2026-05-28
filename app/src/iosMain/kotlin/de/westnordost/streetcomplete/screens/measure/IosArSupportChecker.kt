package de.westnordost.streetcomplete.screens.measure

class IosArSupportChecker : ArSupportChecker {
    // the AR measure app (StreetMeasure) does not exist on iOS, so, measuring with AR
    // is not supported
    override operator fun invoke(): Boolean = false
}
