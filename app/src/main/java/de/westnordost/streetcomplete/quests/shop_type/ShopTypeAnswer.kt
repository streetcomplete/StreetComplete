package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.osmfeatures.Feature

sealed interface ShopTypeAnswer

data object IsShopVacant : ShopTypeAnswer
data class ShopType(val feature: Feature) : ShopTypeAnswer
