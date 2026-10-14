package de.westnordost.streetcomplete.quests.toilets_fee

sealed interface ToiletFeeAnswer
data class ToiletFee(val fee: Boolean) : ToiletFeeAnswer
data object ToiletFeeForNonCustomers : ToiletFeeAnswer
