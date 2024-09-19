package de.westnordost.streetcomplete.quests.sauna_availability

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddSaunaAvailability : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
        (
          leisure ~ fitness_centre
          or leisure = sports_hall and sport = swimming
          or tourism ~ camp_site|hotel
        )
        and !sauna
    """
    override val changesetComment = "Survey sauna availabilities"
    override val wikiLink = "Key:sauna"
    override val icon = R.drawable.ic_quest_sauna
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_saunaAvailability_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["sauna"] = answer.toYesNo()
    }
}
