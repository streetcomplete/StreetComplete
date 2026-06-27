package de.westnordost.streetcomplete.quests.camping

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddCampSiteFee : OsmFilterQuestType<Boolean>(), AndroidQuest {

    /* Most camp sites are paid by default, so asking everywhere would be spammy. We only ask
     * where free access is plausible: basic camp sites and backcountry sites. We only resurvey
     * fee = yes and fee = no, as it might have more detailed values from other editors, and we
     * don't want to damage them. */
    override val elementFilter = """
        nodes, ways with
          tourism = camp_site
          and (camp_site = basic or backcountry = yes)
          and !fee:conditional
          and (
            !fee
            or fee older today -4 years and fee ~ yes|no
          )
    """
    override val changesetComment = "Specify whether you have to pay to camp here"
    override val wikiLink = "Key:fee"
    override val icon = R.drawable.quest_fee
    override val title = Res.string.quest_camp_site_fee_title
    override val achievements = listOf(OUTDOORS)

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("fee", answer.toYesNo())
    }
}
