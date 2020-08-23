package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningHoursRuleList


sealed class FeeAnswer

object HasFee : FeeAnswer()
object HasNoFee : FeeAnswer()
data class HasFeeAtHours(val openingHours: OpeningHoursRuleList) : FeeAnswer()
data class HasFeeExceptAtHours(val openingHours: OpeningHoursRuleList) : FeeAnswer()
