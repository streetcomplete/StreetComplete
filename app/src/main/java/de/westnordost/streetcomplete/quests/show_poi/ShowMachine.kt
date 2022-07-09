package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowMachine : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways with
          amenity ~ vending_machine|atm|telephone|charging_station|device_charging_station
          or atm = yes and (amenity or shop)
    """
    override val changesetComment = "Change vending machine or similar"
    override val wikiLink = "Tag:amenity"
    override val icon = R.drawable.ic_quest_poi_machine
    override val dotColor = "blue"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_machine

    override fun getTitle(tags: Map<String, String>) =
        if (!tags["atm"].isNullOrEmpty() && tags["atm"] != "no")
            R.string.quest_poi_has_atm_title
        else if (tags["amenity"].equals("vending_machine"))
            R.string.quest_poi_vending_title
        else
            R.string.quest_poi_machine_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> {
        return if (tags["amenity"].equals("vending_machine"))
            arrayOf(tags["vending"] ?: tags.entries.toString())
        else
            arrayOf("<no vending tag>")
    }

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
    }
}
