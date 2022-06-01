package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.RARE
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddFerryAccessPedestrian : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "ways, relations with route = ferry and !foot"
    override val changesetComment = "Specify ferry access for pedestrians"
    override val wikiLink = "Tag:route=ferry"
    override val icon = R.drawable.ic_quest_ferry_pedestrian
    override val hasMarkersAtEnds = true
    override val questTypeAchievements = listOf(RARE, PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_ferry_pedestrian_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["foot"] = answer.toYesNo()
    }
}
