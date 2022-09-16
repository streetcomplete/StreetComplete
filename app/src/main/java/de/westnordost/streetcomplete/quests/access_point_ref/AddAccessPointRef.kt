package de.westnordost.streetcomplete.quests.access_point_ref

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class AddAccessPointRef : OsmFilterQuestType<AccessPointRefAnswer>() {

    override val elementFilter = """
        nodes with
        (
          (highway = emergency_access_point)
          or
          (emergency = access_point)
        )
        and !name and !ref and noref != yes and ref:signed != no and !~"ref:.*"
    """

    override val changesetComment = "Determine emergency access point refs"
    override val wikiLink = "Tag:emergency=access_point"
    override val icon = R.drawable.ic_quest_access_point
    override val achievements = listOf(EditTypeAchievement.LIFESAVER)
    override val isDeleteElementEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_accessPointRef_title

    override fun createForm() = AddAccessPointRefForm()

    override fun applyAnswerTo(answer: AccessPointRefAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is NoAccessPointRef -> tags["noref"] = "yes"
            is AccessPointRef ->   tags["ref"] = answer.ref
            is IsAssemblyPointAnswer -> {
                tags["emergency"] = "assembly_point"
                // TODO: This will remove any highway-tag! To be refined to highway=emergency_access_point only.
                tags.remove("highway")
            }
        }
    }
}
