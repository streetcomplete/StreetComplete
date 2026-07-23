package de.westnordost.streetcomplete.data.overlay

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_access_point
import de.westnordost.streetcomplete.resources.quest_address_title
import org.jetbrains.compose.resources.DrawableResource

open class TestOverlayA : Overlay {

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, OverlayStyle>> = sequenceOf()
    override val changesetComment = "test"
    override val icon = Res.drawable.quest_access_point
    override val title = Res.string.quest_address_title
    override val wikiLink = null
    override val achievements = emptyList<EditTypeAchievement>()

    @Composable
    override fun Form(
        on: (OverlayAction) -> Unit,
        element: Element?,
        geometry: ElementGeometry,
        countryInfo: CountryInfo,
        onPinPosition: (icon: DrawableResource, position: LatLon?) -> Unit
    ) {}

}

class TestOverlayB : TestOverlayA()
class TestOverlayC : TestOverlayA()
