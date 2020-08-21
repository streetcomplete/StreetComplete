package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.quests.opening_hours.model.RegularOpeningHours

sealed class FeeAnswer

object HasFee : FeeAnswer()
object HasNoFee : FeeAnswer()
data class HasFeeAtHours(val openingHours: RegularOpeningHours) : FeeAnswer()
data class HasFeeExceptAtHours(val openingHours: RegularOpeningHours) : FeeAnswer()
