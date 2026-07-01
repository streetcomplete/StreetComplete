package de.westnordost.streetcomplete.data.osm.osmquests

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource

open class TestQuestType : OsmElementQuestType<String> {

    override fun isApplicableTo(element: Element): Boolean? = null
    @Composable override fun Form(
        on: (QuestAction<String>) -> Unit,
        element: Element,
        geometry: ElementGeometry,
        countryInfo: CountryInfo, ) {}
    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {}
    override val changesetComment = ""
    override val icon = Res.drawable.quest_bridge
    override val title = Res.string.quest_address_title
    override fun getApplicableElements(mapData: MapDataWithGeometry) = emptyList<Element>()
    override val wikiLink: String? = null
    override val achievements = emptyList<EditTypeAchievement>()
}
