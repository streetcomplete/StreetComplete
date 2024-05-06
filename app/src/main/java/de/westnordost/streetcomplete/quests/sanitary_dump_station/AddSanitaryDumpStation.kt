package de.westnordost.streetcomplete.quests.sanitary_dump_station

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddSanitaryDumpStation : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
         (
           tourism = caravan_site
           or tourism = camp_site and caravans = yes and !backcountry
         )
         and access !~ private|no
         and !sanitary_dump_station
    """

    override val changesetComment = "Specify if there is a sanitary dump station at camp or caravan site"
    override val wikiLink = "Key:sanitary_dump_station"
    override val icon = R.drawable.ic_quest_caravan
    override val achievements = listOf(EditTypeAchievement.OUTDOORS)

    override val hint = R.string.quest_sanitary_dump_station_description
    override val hintImages = listOf(
        R.drawable.sanitary_dump_station_sign1,
        R.drawable.sanitary_dump_station_sign2
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sanitary_dump_station_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["sanitary_dump_station"] = answer.toYesNo()
    }
}
