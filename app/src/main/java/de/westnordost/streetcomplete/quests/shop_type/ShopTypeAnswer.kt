package de.westnordost.streetcomplete.quests.shop_type

sealed class ShopTypeAnswer

object IsShopVacant : ShopTypeAnswer()
data class ShopType(val tags: Map<String, String>) : ShopTypeAnswer()
