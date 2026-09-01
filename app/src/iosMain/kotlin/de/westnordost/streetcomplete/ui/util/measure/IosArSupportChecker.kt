package de.westnordost.streetcomplete.ui.util.measure

class IosArSupportChecker : ArSupportChecker {
    // TODO(multiplatform): Enable AR measurement if StreetMeasure publishes an iOS integration
    // protocol. The existing feature delegates the measurement to that external app, which is
    // Android-only; iOS has no compatible result-producing application to launch.
    override operator fun invoke(): Boolean = false
}
