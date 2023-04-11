package de.westnordost.streetcomplete.overlays.address

import de.westnordost.countryboundaries.CountryBoundaries
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
import de.westnordost.streetcomplete.util.ktx.getIds
import java.util.concurrent.FutureTask

class AddressOverlay(
    private val countryBoundaries: FutureTask<CountryBoundaries>
) : Overlay {

    override val title = R.string.overlay_addresses
    override val icon = R.drawable.ic_quest_housenumber
    override val changesetComment = "Survey housenumbers"
    override val wikiLink: String = "Key:addr"
    override val achievements = listOf(POSTMAN)
    override val hidesQuestTypes = setOf(AddHousenumber::class.simpleName!!)
    override val isCreateNodeEnabled = true

    override val sceneUpdates = listOf(
        "layers.housenumber-labels.enabled" to "false",
        "layers.buildings.draw.buildings-style.extrude" to "false",
        "layers.buildings.draw.buildings-outline-style.extrude" to "false"
    )

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
            .map { it to PointStyle(icon = null, label = getShortHouseNumber(it.tags) ?: "◽") } + // or ▫
        mapData
            .filter("ways, relations with building")
            .filter {
                val center = mapData.getGeometry(it.type, it.id)?.center ?: return@filter false
                val country = countryBoundaries.get().getIds(center).firstOrNull()
                country !in noAddressesOnBuildings
            }
            .map { it to PolygonStyle(Color.INVISIBLE, label = getShortHouseNumber(it.tags)) }

    override fun createForm(element: Element?) = AddressOverlayForm()
}
