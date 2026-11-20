package de.westnordost.streetcomplete.quests.fee

sealed interface FeeAnswer
data class Fee(val fee: Boolean) : FeeAnswer
data object FeeForNonCustomers : FeeAnswer
