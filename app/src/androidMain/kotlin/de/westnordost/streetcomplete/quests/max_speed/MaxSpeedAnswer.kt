package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.osm.maxspeed.Speed

sealed interface MaxSpeedAnswer

data class MaxSpeedSign(val value: Speed) : MaxSpeedAnswer
data class MaxSpeedZone(val value: Speed, val countryCode: String, val roadType: String) : MaxSpeedAnswer
data class AdvisorySpeedSign(val value: Speed) : MaxSpeedAnswer
data class ImplicitMaxSpeed(val countryCode: String, val roadType: String, val lit: Boolean?) : MaxSpeedAnswer
data object IsLivingStreet : MaxSpeedAnswer
