package de.westnordost.streetcomplete.quests.general_fee

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddGeneralFee : OsmFilterQuestType<Boolean>(), AndroidQuest {

    // Bicycle charging station are amenity=charging_station with bicycle=yes/designated.
    // We further exclude dual use charging stations as payment may be waived only for cyclists increasing risk of incorrect answers
    override val elementFilter = """
        nodes, ways with
         (
           (
             amenity = charging_station
             and bicycle ~ yes|designated
             and (!motorcar or motorcar = no)
             and (!motorcycle or motorcycle = no)
             and (!truck or truck = no)
           )
           or tourism ~ museum|gallery|caravan_site|zoo|aquarium
           or leisure ~ beach_resort|disc_golf_course
           or amenity ~ sanitary_dump_station|shower|water_point|public_bath
         )
         and access !~ private|no
         and !fee
    """
    override val changesetComment = "Specify whether places take fees to visit"
    override val wikiLink = "Key:fee"
    override val icon = R.drawable.ic_quest_fee
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) =
        if (tags["amenity"] != null) {
            R.string.quest_generalFee_title
        } else {
            R.string.quest_generalFee_title2
        }

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["fee"] = answer.toYesNo()
    }
}
