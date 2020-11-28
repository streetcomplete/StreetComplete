package de.westnordost.streetcomplete.quests.shop_gone

import de.westnordost.osmfeatures.Feature

sealed class ShopGoneAnswer

object ShopVacant : ShopGoneAnswer()
data class ShopReplaced(val feature: Feature) : ShopGoneAnswer()
object LeaveNote : ShopGoneAnswer()
