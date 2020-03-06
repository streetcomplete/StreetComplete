package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow

sealed class FeeAnswer

object HasFee : FeeAnswer()
object HasNoFee : FeeAnswer()
data class HasFeeAtHours(val hours:List<OpeningMonthsRow>) : FeeAnswer()
data class HasFeeExceptAtHours(val hours:List<OpeningMonthsRow>) : FeeAnswer()
