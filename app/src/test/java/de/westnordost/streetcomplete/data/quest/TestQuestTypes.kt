package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm

open class TestQuestTypeA : OsmElementQuestType<String> {

    override fun getTitle(tags: Map<String, String>) = 0
    override fun isApplicableTo(element: Element): Boolean? = null
    override fun applyAnswerTo(answer: String, tags: Tags, timestampEdited: Long) {}
    override fun createForm(): AbstractOsmQuestForm<String> = object : AbstractOsmQuestForm<String>() {}
    override val changesetComment = "test me"
    override fun getApplicableElements(mapData: MapDataWithGeometry) = mapData.filter { isApplicableTo(it) == true }
    override val wikiLink: String? = null
    override val icon = 0
    override val achievements = emptyList<EditTypeAchievement>()
}

class TestQuestTypeB : TestQuestTypeA()
class TestQuestTypeC : TestQuestTypeA()
class TestQuestTypeD : TestQuestTypeA()
class TestQuestTypeE : TestQuestTypeA()

class TestQuestTypeDisabled : TestQuestTypeA() {
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside
}
