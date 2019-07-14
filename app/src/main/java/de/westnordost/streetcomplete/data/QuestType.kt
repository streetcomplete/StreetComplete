package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment

interface QuestType<T> {

    /** the icon resource id used to display this quest type on the map */
    val icon: Int

    /** returns title resource for when the element has the specified [tags]. The tags are unmodifiable */
    fun getTitle(tags: Map<String, String>): Int

    /** returns on or more replacements, to be used in the title template. The tags are unmodifiable. */
    fun getTitleReplacements(tags: Map<String, String>, typeName: Lazy<String?>): Array<String?>

    /** returns the string resource id that explains why this quest is disabled by default or zero
     * if it is not disabled by default */
    val defaultDisabledMessage: Int get() = 0

    /** returns the dialog in which the user can add the data */
    fun createForm(): AbstractQuestAnswerFragment<T>
}
