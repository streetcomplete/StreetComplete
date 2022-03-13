package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.quest.AllCountries
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.ktx.arrayOfNotNull

/** Quest type where each quest refers to an OSM element */
interface OsmElementQuestType<T> : QuestType<T> {

    /** the changeset comment to be used for this quest type */
    val changesetComment: String

    /** the OpenStreetMap wiki page with the documentation */
    val wikiLink: String?

    /** in which countries the quest should be shown */
    val enabledInCountries: Countries get() = AllCountries

    /** returns whether the markers should be at the ends instead of the center. For example,
     *  street name signs are expected to be at the ends of the street, so the pins should be
     *  there. For the street surface, it is necessary to view the whole street, so it makes sense
     *  if the pins are in the middle */
    val hasMarkersAtEnds: Boolean get() = false

    /** returns whether the user should be able to split the way instead */
    val isSplitWayEnabled: Boolean get() = false

    /** returns whether the user should be able to delete this element instead. Only elements that
     *  are not expected...
     *  - to be part of a relation
     *  - to be part of a network (e.g. roads, power lines, ...)
     *  - to house a second POI on the same element
     *  - to be a kind of element where deletion is not recommended, (e.g. a shop should rather
     *    be set to disused:shop=yes until there is another one)
     *  ...should be deletable */
    val isDeleteElementEnabled: Boolean get() = false

    /** returns whether the user should be able to replace this element with another preset. Only
     *  elements that are expected to be some kind of shop/amenity should be replaceable this way,
     *  i.e. anything that when it's gone, there is a vacant shop then.
     *  */
    val isReplaceShopEnabled: Boolean get() = false

    override val title: Int get() = getTitle(emptyMap())

    /** returns title resource for when the element has the specified [tags]. The tags are unmodifiable */
    fun getTitle(tags: Map<String, String>): Int

    fun getTitleArgs(tags: Map<String, String>): Array<String> = arrayOf()

    /** return all elements within the given map data that are applicable to this quest type. */
    fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element>

    /** returns whether a quest of this quest type could be created out of the given [element]. If the
     * element alone does not suffice to find this out (but e.g. is determined by the data around
     * it), this should return null.
     *
     * The implications of returning null here is that the quest controller needs to fetch a
     * bounding box around the given element (from the database) to determine it is applicable or
     * not. */
    fun isApplicableTo(element: Element): Boolean?

    /** Elements that should be highlighted on the map alongside the selected one because they
     *  provide context for the given element. For example, nearby benches should be shown when
     *  answering a question for a bench so the user knows which of the benches is meant. */
    fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> = emptySequence()

    /** The radius in which certain elements should be shown (see getHighlightedElements).
     *  30m is the default because this is about "across this large street". There shouldn't be
     *  any mix-ups that far apart */
    val highlightedElementsRadius: Double get() = 30.0

    /** applies the data from [answer] to the element that has last been edited at [timestampEdited].
     * The element is not directly modified, instead, a map of [tags] is built */
    fun applyAnswerTo(answer: T, tags: Tags, timestampEdited: Long)

    @Suppress("UNCHECKED_CAST")
    fun applyAnswerToUnsafe(answer: Any, tags: Tags, timestampEdited: Long) {
        applyAnswerTo(answer as T, tags, timestampEdited)
    }
}

typealias Tags = StringMapChangesBuilder
