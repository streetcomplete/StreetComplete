package de.westnordost.streetcomplete.quests.max_speed

sealed class MaxSpeedAnswer

data class MaxSpeedSign(val value: SpeedMeasure) : MaxSpeedAnswer()
data class MaxSpeedZone(val value: SpeedMeasure, val countryCode: String, val roadType: String) : MaxSpeedAnswer()
data class AdvisorySpeedSign(val value: SpeedMeasure) : MaxSpeedAnswer()
data class ImplicitMaxSpeed(val countryCode: String, val roadType: String) : MaxSpeedAnswer()
object IsLivingStreet : MaxSpeedAnswer()
