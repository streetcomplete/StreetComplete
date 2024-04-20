package de.westnordost.streetcomplete.quests.moped

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class AddProhibitedForMoped : OsmFilterQuestType<AddMopedAccessAnswer>() {

    override val elementFilter =
        //only include separate cycleways,
        // in case of a cycleway that is part of a road, mopeds are assumed to be allowed on the road
        """
        ways with (
            highway = cycleway
        )
        and !moped
        and (motor_vehicle != no or !motor_vehicle)
        """
    override val enabledInCountries = NoCountriesExcept("BE")
    override val defaultDisabledMessage = R.string.default_disabled_msg_visible_sign_moped


    override val changesetComment = "Specify if a moped is allowed on the cycleway"
    override val wikiLink = "Key:moped"
    override val icon = R.drawable.ic_quest_moped_access

    override val achievements = listOf(EditTypeAchievement.BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_moped_access_title

    override fun createForm() = AddMopedAccessForm()

    override fun applyAnswerTo(
        answer: AddMopedAccessAnswer,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["moped"] = when (answer) {
            AddMopedAccessAnswer.ALLOWED ->  "yes"
            AddMopedAccessAnswer.FORBIDDEN ->  "no"
            AddMopedAccessAnswer.DESIGNATED ->  "designated"
        }
    }
}
