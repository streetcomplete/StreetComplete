package de.westnordost.streetcomplete.quests.opening_hours

object Assert {
    fun assert(conditionAssertedToBeTrue: Boolean) {
        if(!conditionAssertedToBeTrue) {
            throw AssertionError()
        }
    }
}
