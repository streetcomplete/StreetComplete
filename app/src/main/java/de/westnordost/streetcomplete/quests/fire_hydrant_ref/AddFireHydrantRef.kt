package de.westnordost.streetcomplete.quests.fire_hydrant_ref

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class AddFireHydrantRef : OsmFilterQuestType<FireHydrantRefAnswer>() {

    override val elementFilter = """
        nodes with
        emergency = fire_hydrant
        and !name and !ref and noref != yes and ref:signed != no and !~"ref:.*"
    """
    override val changesetComment = "Determine fire hydrant refs"
    override val wikiLink = "Key:ref"
    override val icon = R.drawable.ic_quest_fire_hydrant_ref
    override val achievements = listOf(EditTypeAchievement.LIFESAVER)
    override val isDeleteElementEnabled = true
    override val enabledInCountries = NoCountriesExcept(
        "CH", "FR"
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_genericRef_title

    override fun createForm() = AddFireHydrantRefForm()

    override fun applyAnswerTo(answer: FireHydrantRefAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoVisibleFireHydrantRef -> tags["ref:signed"] = "no"
            is FireHydrantRef ->          tags["ref"] = answer.ref
        }
    }
}
