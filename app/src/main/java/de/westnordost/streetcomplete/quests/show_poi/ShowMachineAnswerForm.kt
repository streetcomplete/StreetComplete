package de.westnordost.streetcomplete.quests.show_poi

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class ShowMachineAnswerForm : AbstractOsmQuestForm<Boolean>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (element.tags["amenity"] != "vending_machine") return
        val vending = element.tags["vending"] ?: return
        setTitle(resources.getString((questType as OsmElementQuestType<*>).getTitle(element.tags)) + " $vending")
    }

}
