package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment

interface QuestType<T> {

    /** the icon resource id used to display this quest type on the map */
    val icon: Int

    /** the title resource id used to display the quest's question */
    val title: Int

    /** returns on or more replacements, to be used in the title template. The tags are unmodifiable. */
    fun getTitleReplacements(tags: Map<String, String>, featureName: Lazy<String?>): Array<String?>

    /** returns the string resource id that explains why this quest is disabled by default or zero
     * if it is not disabled by default */
    val defaultDisabledMessage: Int get() = 0

    /** returns the dialog in which the user can add the data */
    fun createForm(): AbstractQuestAnswerFragment<T>
}
