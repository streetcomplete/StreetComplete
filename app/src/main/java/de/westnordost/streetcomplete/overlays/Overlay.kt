package de.westnordost.streetcomplete.overlays

import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry

/** An overlay is displayed on top of the normal map but behind quest pins and visualizes how
 *  selected data is tagged. Tapping on an element can optionally open a form in which the user
 *  can answer a question, just like with quests */
interface Overlay : ElementEditType {
    /** which quest types (by name) should not be visible when this overlay is active */
    val hidesQuestTypes: Set<String> get() = emptySet()

    /** scene updates that should be applied to the map when this overlay is active */
    val sceneUpdates: List<Pair<String, String>>? get() = null

    val isCreateNodeEnabled: Boolean get() = false

    /** return pairs of element to style for all elements in the map data that should be displayed */
    fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>>

    /** returns the fragment in which the user can view/add the data or null if no form should be
     * displayed for the given [element]. [element] is null for when a new element should be created
     * */
    fun createForm(element: Element?): AbstractOverlayForm?
}
