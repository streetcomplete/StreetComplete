package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.edits.ElementEditType

interface OsmCreateElementQuestType<T> : QuestType, ElementEditType {
    /** The radius in which certain elements should be shown (see getHighlightedElements).
     *  30m is the default because this is about "across this large street". There shouldn't be
     *  any misunderstandings which element is meant that far apart. */
    // TODO: highlightedElementsRadius and its comment duplicates OsmElementQuestType entry: should it be moved higher? Into interface? This does not apply to Note quests
    val highlightedElementsRadius: Double get() = 30.0
}
