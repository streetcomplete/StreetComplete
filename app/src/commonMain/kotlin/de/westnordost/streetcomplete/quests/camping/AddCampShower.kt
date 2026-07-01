package de.westnordost.streetcomplete.quests.camping

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddCampShower : OsmFilterQuestType<Boolean>() {

    /* We only resurvey shower = yes and shower = no, as it might have more detailed
     * values from other editors, and we don't want to damage them */
    override val elementFilter = """
        nodes, ways with
          (
            tourism ~ camp_site|alpine_hut|wilderness_hut|caravan_site
            or leisure ~ bathing_place|marina
            or highway = services and toilets = yes
            or amenity = public_bath and fee = no
          ) and (
            !shower
            or shower older today -4 years and shower ~ yes|no
          )
    """
    override val changesetComment = "Specify whether there are showers available"
    override val wikiLink = "Key:shower"
    override val icon = Res.drawable.quest_shower
    override val title = Res.string.quest_camp_shower_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with tourism = camp_site")

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["shower"] = answer.toYesNo()
    }
}
