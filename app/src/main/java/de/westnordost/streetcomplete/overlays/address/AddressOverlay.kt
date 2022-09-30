package de.westnordost.streetcomplete.overlays.address

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.quests.address.AddHousenumber
import de.westnordost.streetcomplete.util.getShortHouseNumber

class AddressOverlay : Overlay {

    override val title = R.string.overlay_addresses
    override val icon = R.drawable.ic_quest_housenumber
    override val changesetComment = "Add housenumbers"
    override val wikiLink: String = "Key:addr"
    override val achievements = listOf(POSTMAN)
    override val hidesQuestTypes = setOf(AddHousenumber::class.simpleName!!)
    override val isCreateNodeEnabled = true

    override val sceneUpdates = listOf(
        "layers.housenumber-labels.enabled" to "false",
        "layers.buildings.draw.buildings-style.extrude" to "false",
        "layers.buildings.draw.buildings-outline-style.extrude" to "false"
    )

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData.filter("""
            nodes with
              addr:housenumber or addr:housename or addr:conscriptionnumber or addr:streetnumber
        """).map { it to PointStyle(icon = null, label = getShortHouseNumber(it.tags)) } +
        mapData.filter("""
            ways, relations with building
        """).map {
            it to PolygonStyle(Color.INVISIBLE, label = getShortHouseNumber(it.tags))
        }

    override fun createForm(element: Element?) = AddressOverlayForm()
}
