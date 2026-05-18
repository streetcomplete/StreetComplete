package de.westnordost.streetcomplete.quests.toilet_availability

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddToiletAvailability : OsmFilterQuestType<Boolean>() {

    // only for malls, big stores and rest areas because users should not need to go inside a non-public
    // place to solve the quest. (Considering malls and department stores public enough)
    override val elementFilter = """
        nodes, ways with
        (
          shop ~ mall|department_store
          or highway ~ services|rest_area
          or tourism ~ camp_site|caravan_site|wilderness_hut
          or leisure = bathing_place
        )
        and !toilets
    """
    override val changesetComment = "Survey toilet availabilities"
    override val wikiLink = "Key:toilets"
    override val icon = R.drawable.quest_toilets
    override val title = Res.string.quest_toiletAvailability_title
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(onAnswer: (Boolean) -> Unit, element: Element) {
        YesNoQuestForm(onAnswer)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["toilets"] = answer.toYesNo()
    }
}
