package de.westnordost.streetcomplete.overlays.address

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.AndroidOverlay
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.quests.address.AddHousenumber
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.getShortHouseNumber

class AddressOverlay(
    private val getCountryInfoByLocation: (location: LatLon) -> CountryInfo
) : Overlay, AndroidOverlay {

    override val title = Res.string.overlay_addresses
    override val icon = R.drawable.quest_housenumber
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
                val center = mapData.getGeometry(it.type, it.id)?.center
                val countryCode = center?.let { getCountryInfoByLocation(center).countryCode }
                val label = getShortHouseNumber(it.tags, countryCode) // or ▫
                val icon = if (label != null) R.drawable.ic_address_dot else null
                it to OverlayStyle.Point(icon = icon, label = label ?: "◽")
            } +
        mapData
            .filter("ways, relations with building")
            .mapNotNull {
                val center = mapData.getGeometry(it.type, it.id)?.center ?: return@mapNotNull null
                val countryCode = getCountryInfoByLocation(center).countryCode
                if (countryCode in noAddressesOnBuildings) return@mapNotNull null

                val label = getShortHouseNumber(it.tags, countryCode)
                val color = if (label != null) OverlayColor.Blue else OverlayColor.Invisible
                it to OverlayStyle.Polygon(color = color, label = label)
            }

    override fun createForm(element: Element?) = AddressOverlayForm()
}
