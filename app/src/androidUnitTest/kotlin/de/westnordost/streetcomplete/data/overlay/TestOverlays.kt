package de.westnordost.streetcomplete.data.overlay

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement

open class TestOverlayA : Overlay {
    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, OverlayStyle>> = sequenceOf()
    override val changesetComment: String = "test"
    override val icon: Int = 0
    override val title: Int = 0
    override val wikiLink: String? = null
    override val achievements: List<EditTypeAchievement> = emptyList()
}

class TestOverlayB : TestOverlayA()
class TestOverlayC : TestOverlayA()
