package de.westnordost.streetcomplete.quests.aerialway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.aerialway.AerialwayBicycleAccessAnswer.YES
import de.westnordost.streetcomplete.quests.aerialway.AerialwayBicycleAccessAnswer.SUMMER
import de.westnordost.streetcomplete.quests.aerialway.AerialwayBicycleAccessAnswer.NO_SIGN
import de.westnordost.streetcomplete.quests.aerialway.AerialwayBicycleAccessAnswer.NO

class AddAerialwayBicycleAccess : OsmFilterQuestType<AerialwayBicycleAccessAnswer>(), AndroidQuest {

    override val elementFilter = """
        ways with
          aerialway ~ cable_car|gondola|chair_lift
          and !aerialway:bicycle and !bicycle
    """

    override val changesetComment = "Specify whether bicycles can be taken on aerialway"
    override val wikiLink = "Tag:aerialway"
    override val icon = R.drawable.quest_aerialway_bicycle
    override val achievements = listOf(RARE, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_aerialway_bicycle_title

    override fun createForm() = AerialwayBicycleAccessForm()

    override fun applyAnswerTo(answer: AerialwayBicycleAccessAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            YES -> tags["aerialway:bicycle"] = "yes"
            SUMMER -> tags["aerialway:bicycle"] = "summer"
            NO -> tags["aerialway:bicycle"] = "no"
            NO_SIGN -> tags["aerialway:bicycle:signed"] = "no"
        }
    }
}
