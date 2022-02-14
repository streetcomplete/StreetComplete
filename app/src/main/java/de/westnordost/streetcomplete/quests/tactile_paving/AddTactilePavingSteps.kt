package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_PAVED
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BLIND
import de.westnordost.streetcomplete.ktx.toYesNo

class AddTactilePavingSteps : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        ways with highway = steps
         and surface ~ ${ANYTHING_PAVED.joinToString("|")}
         and (!indoor or indoor = no)
         and access !~ private|no
        and (
          !tactile_paving
          or tactile_paving = unknown
          or tactile_paving = no and tactile_paving older today -4 years
          or tactile_paving = yes and tactile_paving older today -8 years
        )
    """

    override val changesetComment = "Add tactile paving on steps"
    override val wikiLink = "Key:tactile_paving"
    override val icon = R.drawable.ic_quest_steps_tactile_paving
    override val enabledInCountries = COUNTRIES_WHERE_TACTILE_PAVING_IS_COMMON

    override val questTypeAchievements = listOf(BLIND)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tactile_paving_steps_title

    override fun createForm() = TactilePavingForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("tactile_paving", answer.toYesNo())
    }
}
