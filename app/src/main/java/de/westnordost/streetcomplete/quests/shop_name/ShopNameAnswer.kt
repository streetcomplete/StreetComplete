package de.westnordost.streetcomplete.quests.shop_name

sealed class ShopNameAnswer

data class ShopName(val name:String) : ShopNameAnswer()
object NoShopNameSign : ShopNameAnswer()
