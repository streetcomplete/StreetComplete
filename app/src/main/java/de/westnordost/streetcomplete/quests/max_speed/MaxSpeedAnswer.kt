package de.westnordost.streetcomplete.quests.max_speed

sealed class MaxSpeedAnswer

data class MaxSpeedSign(val value: String) : MaxSpeedAnswer()
data class MaxSpeedZone(val value: String, val countryCode: String, val roadType: String) : MaxSpeedAnswer()
data class AdvisorySpeedSign(val value: String) : MaxSpeedAnswer()
data class ImplicitMaxSpeed(val countryCode: String, val roadType: String) : MaxSpeedAnswer()
object IsLivingStreet : MaxSpeedAnswer()
