package de.westnordost.streetcomplete.quests.shop_gone

sealed class ShopGoneAnswer

object ShopVacant : ShopGoneAnswer()
data class ShopReplaced(val tags: Map<String, String>) : ShopGoneAnswer()
object LeaveNote : ShopGoneAnswer()
