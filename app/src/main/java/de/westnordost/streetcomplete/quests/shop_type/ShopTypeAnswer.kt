package de.westnordost.streetcomplete.quests.shop_type

sealed interface ShopTypeAnswer

data object IsShopVacant : ShopTypeAnswer
data class ShopType(val tags: Map<String, String>) : ShopTypeAnswer
