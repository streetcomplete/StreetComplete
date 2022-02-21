package de.westnordost.streetcomplete.quests.max_speed

sealed class MaxSpeedAnswer

data class MaxSpeedSign(val value: Speed) : MaxSpeedAnswer()
data class MaxSpeedZone(val value: Speed, val countryCode: String, val roadType: String) : MaxSpeedAnswer()
data class AdvisorySpeedSign(val value: Speed) : MaxSpeedAnswer()
data class ImplicitMaxSpeed(val countryCode: String, val roadType: String, val lit: Boolean?) : MaxSpeedAnswer()
object IsLivingStreet : MaxSpeedAnswer()
