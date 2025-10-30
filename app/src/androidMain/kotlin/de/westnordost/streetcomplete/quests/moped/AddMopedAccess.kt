package de.westnordost.streetcomplete.quests.moped

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_visible_sign_moped

class AddMopedAccess : OsmFilterQuestType<MopedAccessAnswer>(), AndroidQuest {

    override val elementFilter = """
        ways with (
            highway = cycleway
            or highway = path and bicycle = designated
            or highway = footway and bicycle = designated
        )
        and !moped
        and !moped:signed
        and (motor_vehicle != no or !motor_vehicle)
    """
    override val enabledInCountries = NoCountriesExcept(
        "BE", // https://github.com/streetcomplete/StreetComplete/issues/5565
        "SE" // https://github.com/streetcomplete/StreetComplete/discussions/6482
    )
    override val defaultDisabledMessage = Res.string.default_disabled_msg_visible_sign_moped
    override val changesetComment = "Specify if a moped is allowed on the cycleway"
    override val wikiLink = "Key:moped"
    override val icon = R.drawable.quest_moped_access

    override val achievements = listOf(EditTypeAchievement.BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_moped_access_title

    override fun createForm() = AddMopedAccessForm()

    override fun applyAnswerTo(answer: MopedAccessAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            MopedAccessAnswer.NO_SIGN ->  tags["moped:signed"] = "no"
            MopedAccessAnswer.FORBIDDEN ->  tags["moped"] = "no"
            MopedAccessAnswer.DESIGNATED ->  tags["moped"] = "designated"
        }
    }
}
