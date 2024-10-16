package de.westnordost.streetcomplete.overlays.address

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.quests.address.AddHousenumber
import de.westnordost.streetcomplete.util.getShortHouseNumber

class AddressOverlay(
    private val getCountryCodeByLocation: (location: LatLon) -> String?
) : Overlay {

    override val title = R.string.overlay_addresses
    override val icon = R.drawable.ic_quest_housenumber
    override val changesetComment = "Survey housenumbers"
    override val wikiLink: String = "Key:addr"
    override val achievements = listOf(POSTMAN)
    override val hidesQuestTypes = setOf(AddHousenumber::class.simpleName!!)
    override val isCreateNodeEnabled = true

    override val hidesLayers = listOf("labels-housenumbers")

    private val noAddressesOnBuildings = setOf(
        "IT" // https://github.com/streetcomplete/StreetComplete/issues/4801
    )

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData
            .filter("""
                nodes with
                  addr:housenumber or addr:housename or addr:conscriptionnumber or addr:streetnumber
                  or entrance
            """)
            .map {
                val label = getShortHouseNumber(it.tags) // or ▫
                val icon = if (label != null) R.drawable.ic_address_dot else null
                it to PointStyle(icon = icon, label = label ?: "◽")
            } +
        mapData
            .filter("ways, relations with building")
            .filter {
                val center = mapData.getGeometry(it.type, it.id)?.center ?: return@filter false
                val country = getCountryCodeByLocation(center)
                country !in noAddressesOnBuildings
            }
            .map {
                val label = getShortHouseNumber(it.tags)
                val color = if (label != null) Color.BLUE else Color.INVISIBLE
                it to PolygonStyle(color = color, label = label)
            }

    override fun createForm(element: Element?) = AddressOverlayForm()
}
