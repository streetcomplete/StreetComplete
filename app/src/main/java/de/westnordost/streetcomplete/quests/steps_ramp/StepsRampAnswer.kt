package de.westnordost.streetcomplete.quests.steps_ramp

data class StepsRampAnswer(
    val bicycleRamp: Boolean,
    val strollerRamp: Boolean,
    val wheelchairRamp: WheelchairRampStatus
) {
    fun hasRamp() = bicycleRamp || strollerRamp || wheelchairRamp != WheelchairRampStatus.NO
}

enum class WheelchairRampStatus { YES, NO, SEPARATE }