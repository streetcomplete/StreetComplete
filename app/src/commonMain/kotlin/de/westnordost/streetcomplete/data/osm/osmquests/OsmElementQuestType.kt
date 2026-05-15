package de.westnordost.streetcomplete.data.osm.osmquests

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.quest.AllCountries
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.osm.Tags
import org.jetbrains.compose.resources.StringResource

/** Quest type where each quest refers to one OSM element.
 *
 *  A quest type referring to one OSM element specifies via the [getApplicableElements] and
 *  [isApplicableTo] methods for which OSM elements a quest of this type should be created.
 *  Quest types that do not require complex filters that depend on the geometry of surrounding
 *  elements subclass [OsmFilterQuestType][de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType]
 */
interface OsmElementQuestType<T> : QuestType, ElementEditType {

    /** In which countries the quest should be shown. By default, in all countries.
     *
     *  Use [AllCountriesExcept][de.westnordost.streetcomplete.data.quest.AllCountriesExcept] to exclude some countries.
     *  Use [NoCountriesExcept][de.westnordost.streetcomplete.data.quest.NoCountriesExcept] to exclude all except some countries.
     *
     * A quest type should not be shown in a country if it is either irrelevant/not applicable in
     * that country or if it would not fulfill the [quest guidelines](https://github.com/streetcomplete/StreetComplete/blob/master/QUEST_GUIDELINES.md)
     * in that country only.
     *
     * The reason why a country is excluded or included here should be documented. */
    val enabledInCountries: Countries get() = AllCountries

    /** Whether the markers should be at the ends instead of the center. By default: false.
     *
     * For example, street name signs are expected to be at the ends of the street, so the pins
     * should be there too. For the street surface, it is necessary to view the whole street, so it
     * makes sense if the pins are in the middle. */
    val hasMarkersAtEnds: Boolean get() = false

    /** All elements within the given map data that are applicable to this quest type, i.e. for
     *  which a quest of this type should be created.
     *
     *  This method is called primarily when new OSM data is downloaded. All of the downloaded data
     *  is passed to this method and the function of this method is to filter out and only return
     *  those elements for which a quest should be created. */
    fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element>

    /** Whether a quest of this quest type could be created out of the given [element]. This method
     *  is primarily called when a single element is updated (e.g. by the user giving an answer)
     *  and so it must be re-checked if there should be a quest of this type for this element now
     *  (or not anymore).
     *  If the element alone does not suffice to find this out, null is returned. This is the case
     *  for quest types where whether there is a quest or not for any particular element depends on
     *  the surrounding data, such as what kind of gate there is where a footway and a wall meet.
     *
     * The implications of returning null here is that the quest controller needs to fetch a
     * bounding box around the given element (from the database) to determine it is applicable or
     * not (this is slow). */
    fun isApplicableTo(element: Element): Boolean?

    /** Elements that should be highlighted on the map alongside the selected one because they
     *  provide context for the given element. For example, nearby benches should be shown when
     *  answering a question for a bench so the user knows which of the benches is meant. */
    fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry): Sequence<Element> = emptySequence()

    /** The radius in which certain elements should be shown (see getHighlightedElements).
     *  30m is the default because this is about "across this large street". There shouldn't be
     *  any misunderstandings which element is meant that far apart. */
    val highlightedElementsRadius: Double get() = 30.0

    /** Composable form in which to enter the requested information. Use
     *  [QuestForm][de.westnordost.streetcomplete.ui.common.quest.QuestForm] to define a custom
     *  (or simple button-based) one, or any of the pre-defined generic forms like…
     *  - [ItemSelectQuestForm][de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm]
     *  - [ItemsSelectQuestForm][de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm]
     *  - [RadioGroupQuestForm][de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm]
     *  - [CheckboxGroupQuestForm][de.westnordost.streetcomplete.ui.common.quest.CheckboxGroupQuestForm]
     *  - [YesNoQuestForm][de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm]
     *  - [CountInputQuestForm][de.westnordost.streetcomplete.ui.common.quest.CountInputQuestForm]
     *  - [LocalizedNameQuestForm][de.westnordost.streetcomplete.ui.common.quest.LocalizedNameQuestForm]
     *  - [NameWithSuggestionsQuestForm][de.westnordost.streetcomplete.ui.common.quest.NameWithSuggestionsQuestForm]
     *
     *  A good practice is that if the form definition is purely declarative and there's no mutable
     *  state, it's fine to have the form definition inline in the quest type class, otherwise,
     *  better put it into an own file.
     *  */
    @Composable
    fun Form(onAnswer: (T) -> Unit)

    /** Applies the data from [answer] to the element that has last been edited at [timestampEdited]
     * with the given [tags] and the given [geometry].
     * The element is not directly modified, instead, a map of [tags] is modified */
    fun applyAnswerTo(answer: T, tags: Tags, geometry: ElementGeometry, timestampEdited: Long)
}
