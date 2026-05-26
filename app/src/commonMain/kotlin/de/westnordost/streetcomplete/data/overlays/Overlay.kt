package de.westnordost.streetcomplete.data.overlays

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry

/** An overlay is displayed on top of the normal map but behind quest pins and visualizes how
 *  selected data is tagged. Tapping on an element can optionally open a form in which the user
 *  can answer a question, just like with quests */
interface Overlay : ElementEditType {
    /** which quest types (by name) should not be visible when this overlay is active */
    val hidesQuestTypes: Set<String> get() = emptySet()

    /** layers that should be hidden while this overlay is active */
    val hidesLayers: List<String> get() = emptyList()

    /** Returns a composable form in which to enter the requested information if clicking on
     *  the given element should not do anything. Use
     *  [OverlayForm][de.westnordost.streetcomplete.ui.common.overlay.OverlayForm] to define a
     *  custom one, or any of the pre-defined generic forms like…
     *
     *  - [ItemSelectOverlayForm][de.westnordost.streetcomplete.ui.common.overlay.ItemSelectOverlayForm] -
     *    Select one from a set of (image) items
     *
     *  - [GroupedItemSelectOverlayForm][de.westnordost.streetcomplete.ui.common.overlay.GroupedItemSelectOverlayForm] -
     *    Select one from a grouped list of (image) items
     *  */
    @Composable
    fun Form(
        onEdit: (ElementEditAction) -> Unit,
        element: Element?,
        geometry: ElementGeometry,
        countryInfo: CountryInfo
    )

    /** Whether the form can be opened with a null element (=new element) */
    val isCreateNodeEnabled: Boolean get() = false

    /** return pairs of element to style for all elements in the map data that should be displayed */
    fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, OverlayStyle>>
}
