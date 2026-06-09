package de.westnordost.streetcomplete.ui.util.measure

import de.westnordost.streetcomplete.ui.util.measure.ArSupportChecker

class IosArSupportChecker : ArSupportChecker {
    // the AR measure app (StreetMeasure) does not exist on iOS, so, measuring with AR
    // is not supported
    override operator fun invoke(): Boolean = false
}
