package de.westnordost.streetcomplete.quests.atm_cashin

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddAtmCashIn : OsmFilterQuestType<Boolean>(), AndroidQuest {

    override val elementFilter = "nodes with amenity = atm and !cash_in"
    override val changesetComment = "Determine whether ATM allows depositing cash"
    override val wikiLink = "Key:cash_in"
    override val icon = R.drawable.quest_money
    override val title = Res.string.quest_atm_cashin_title
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with amenity = atm")

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["cash_in"] = answer.toYesNo()
    }
}
