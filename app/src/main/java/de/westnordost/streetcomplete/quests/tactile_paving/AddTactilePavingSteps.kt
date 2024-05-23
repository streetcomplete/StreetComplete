package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.PAVED_SURFACES
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddTactilePavingSteps : OsmFilterQuestType<TactilePavingStepsAnswer>() {

    override val elementFilter = """
        ways with highway = steps
         and surface ~ ${PAVED_SURFACES.joinToString("|")}
         and !sac_scale
         and (!conveying or conveying = no)
         and access !~ private|no
        and (
          !tactile_paving
          or tactile_paving = unknown
          or tactile_paving ~ no|partial|incorrect and tactile_paving older today -4 years
          or tactile_paving = yes and tactile_paving older today -8 years
        )
    """

    override val changesetComment = "Survey tactile paving on steps"
    override val wikiLink = "Key:tactile_paving"
    override val icon = R.drawable.ic_quest_steps_tactile_paving
    override val enabledInCountries = COUNTRIES_WHERE_TACTILE_PAVING_IS_COMMON
    override val achievements = listOf(BLIND)

    override val hint = R.string.quest_generic_looks_like_this
    override val hintImages = listOf(
        R.drawable.tactile_paving1,
        R.drawable.tactile_paving2,
        R.drawable.tactile_paving3
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tactilePaving_title_steps

    override fun createForm() = TactilePavingStepsForm()

    override fun applyAnswerTo(answer: TactilePavingStepsAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("tactile_paving", answer.osmValue)
    }
}
