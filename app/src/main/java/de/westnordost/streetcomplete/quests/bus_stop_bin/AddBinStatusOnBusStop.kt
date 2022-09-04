package de.westnordost.streetcomplete.quests.bus_stop_bin

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddBinStatusOnBusStop : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
        (
          public_transport = platform
          or (highway = bus_stop and public_transport != stop_position)
        )
        and physically_present != no and naptan:BusStopType != HAR
        and (!bin or bin older today -4 years)
    """
    override val changesetComment = "Specify whether public transport stops have bins"
    override val wikiLink = "Key:bin"
    override val icon = R.drawable.ic_quest_bin_public_transport
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_busStopBin_title2

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("bin", answer.toYesNo())
    }
}
