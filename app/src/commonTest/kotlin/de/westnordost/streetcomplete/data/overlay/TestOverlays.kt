package de.westnordost.streetcomplete.data.overlay

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.resources.*

open class TestOverlayA : Overlay {
    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, OverlayStyle>> = sequenceOf()
    override val changesetComment = "test"
    override val icon = 0
    override val title = Res.string.quest_address_title
    override val wikiLink = null
    override val achievements = emptyList<EditTypeAchievement>()
}

class TestOverlayB : TestOverlayA()
class TestOverlayC : TestOverlayA()
