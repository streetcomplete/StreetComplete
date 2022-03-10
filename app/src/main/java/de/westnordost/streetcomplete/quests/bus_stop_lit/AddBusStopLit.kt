package de.westnordost.streetcomplete.quests.bus_stop_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.ktx.containsAnyKey
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddBusStopLit : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes with
        (
          (public_transport = platform and ~bus|trolleybus|tram ~ yes)
          or
          (highway = bus_stop and public_transport != stop_position)
        )
        and physically_present != no and naptan:BusStopType != HAR
        and (
          !lit
          or lit = no and lit older today -8 years
          or lit older today -16 years
        )
    """

    override val changesetComment = "Add whether a bus stop is lit"
    override val wikiLink = "Key:lit"
    override val icon = R.drawable.ic_quest_bus_stop_lit
    override val questTypeAchievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsAnyKey("name", "ref")
        val isTram = tags["tram"] == "yes"
        return when {
            isTram && hasName ->    R.string.quest_busStopLit_tram_name_title
            isTram ->               R.string.quest_busStopLit_tram_title
            hasName ->              R.string.quest_busStopLit_name_title
            else ->                 R.string.quest_busStopLit_title
        }
    }

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> =
        arrayOfNotNull(tags["name"] ?: tags["ref"])

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("lit", answer.toYesNo())
    }
}
