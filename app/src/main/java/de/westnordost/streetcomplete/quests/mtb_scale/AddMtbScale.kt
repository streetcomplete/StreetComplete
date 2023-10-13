package de.westnordost.streetcomplete.quests.mtb_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.NATURAL_SURFACES
import de.westnordost.streetcomplete.osm.smoothness.SMOOTHNESS_BAD_OR_WORSE_BUT_PASSABLE
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddMtbScale : OsmFilterQuestType<MtbScale>() {

    override val elementFilter = """
        ways with highway ~ path|track
        and (
          !mtb:scale
          or mtb:scale older today -8 years
        )
        and (
          surface ~ ${NATURAL_SURFACES.joinToString("|")}
          or smoothness ~ ${SMOOTHNESS_BAD_OR_WORSE_BUT_PASSABLE.joinToString("|")}
        )
        and ((access and access !~ private|no) or (foot and foot !~ private|no) or (bicycle and bicycle !~ private|no))
        and (sidewalk and sidewalk !~ both|left|right)
    """
    override val changesetComment = "Specify MTB difficulty"
    override val wikiLink = "Key:mtb:scale"
    override val icon = R.drawable.ic_quest_mtb
    override val achievements = listOf(BICYCLIST, OUTDOORS)
    override val defaultDisabledMessage = R.string.default_disabled_msg_mtb

    override fun getTitle(tags: Map<String, String>) = R.string.quest_mtb_scale_title

    override fun createForm() = AddMtbScaleForm()

    override fun applyAnswerTo(answer: MtbScale, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("mtb:scale", answer.osmValue)
    }
}
