package de.westnordost.streetcomplete.quests.moped

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class AddProhibitedForMoped : OsmFilterQuestType<ProhibitedForMopedAnswer>() {

    override val elementFilter =
        // filter all cycleways next to a road with a speed of more than 50, as Moped use is designated by law
        """
        ways with (
            highway = cycleway
            or (cycleway and bicycle ~ yes|designated and (maxspeed <= 30 or !maxspeed))
        )
        and !moped
        and (motor_vehicle != no or !motor_vehicle)
        """
    override val enabledInCountries = NoCountriesExcept("BE")
    override val defaultDisabledMessage = R.string.default_disabled_msg_visible_sign_moped


    override val changesetComment = "Specify if a moped is allowed on the cycleway"
    override val wikiLink = "Key:moped"
    override val icon = R.drawable.ic_quest_no_bicycles

    override val achievements = listOf(EditTypeAchievement.BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_moped_prohibited_title

    override fun createForm() = AddProhibitedForMopedForm()

    override fun applyAnswerTo(
        answer: ProhibitedForMopedAnswer,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        when (answer) {
            ProhibitedForMopedAnswer.ALLOWED -> tags["moped"] = "yes"
            ProhibitedForMopedAnswer.FORBIDDEN -> tags["moped"] = "no"
            ProhibitedForMopedAnswer.DESIGNATED -> tags["moped"] = "designated"

        }
    }
}
