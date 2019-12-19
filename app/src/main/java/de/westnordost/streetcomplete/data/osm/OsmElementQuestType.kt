package de.westnordost.streetcomplete.data.osm

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

interface OsmElementQuestType<T> : QuestType<T> {

    fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"]
        return if (name != null) arrayOf(name) else arrayOf()
    }

    /** the commit message to be used for this quest type */
    val commitMessage: String

    // the below could also go up into QuestType interface, but then they should be accounted for
    // in the respective download/upload classes as well

    /** in which countries the quest should be shown */
    val enabledInCountries: Countries get() = AllCountries

    /** returns whether the markers should be at the ends instead of the center */
    val hasMarkersAtEnds: Boolean get() = false

    /** returns whether the user should be able to split the way instead */
    val isSplitWayEnabled: Boolean get() = false

    /** returns title resource for when the element has the specified [tags]. The tags are unmodifiable */
    fun getTitle(tags: Map<String, String>): Int

    override val title: Int get() = getTitle(emptyMap())

    /** Downloads map data for this quest type for the given [bbox] and puts the received data into
     *  the [handler]. Returns whether the download was successful
     */
    fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean

    /** returns whether a quest of this quest type could be created out of the given [element]. If the
     * element alone does not suffice to find this out (but e.g. an Overpass query would need to be
     * made to find this out), this should return null.
     *
     * The implications of returning null here is that this quest will never be created directly
     * as consequence of solving another quest and also after reverting an input, the quest will
     * not immediately pop up again.*/
    fun isApplicableTo(element: Element): Boolean?

    /** applies the data from [answer] to the given element. The element is not directly modified,
     *  instead, a map of [changes] is built */
    fun applyAnswerTo(answer: T, changes: StringMapChangesBuilder)

    fun applyAnswerToUnsafe(answer: Any, changes: StringMapChangesBuilder) {
        applyAnswerTo(answer as T, changes)
    }

    /** The quest type can clean it's metadata here, if any  */
    fun cleanMetadata() {}
}
