package de.westnordost.streetcomplete.quests.steps_ramp

data class StepsRampAnswer(
    val bicycleRamp: Boolean,
    val strollerRamp: Boolean,
    val wheelchairRamp: Boolean
) {
    fun hasRamp() = bicycleRamp || strollerRamp || wheelchairRamp
}