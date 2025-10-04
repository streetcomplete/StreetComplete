package de.westnordost.streetcomplete.quests.boat_lock_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.boat_lock_type.BoatLockType.AUTOMATED
import de.westnordost.streetcomplete.quests.boat_lock_type.BoatLockType.SELF_SERVICE
import de.westnordost.streetcomplete.quests.boat_lock_type.BoatLockType.MANUAL
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddBoatLockType : OsmFilterQuestType<List<BoatLockType>>(), AndroidQuest {

    override val elementFilter = "nodes, ways with lock and !automated and !self_service and !manual"
    override val changesetComment = "Specify boat lock types"
    override val wikiLink = "Tag:lock"
    override val icon = R.drawable.ic_quest_lock_boat
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_boat_lock_type_title

    override fun createForm() = AddBoatLockTypeForm()

    override fun applyAnswerTo(answer: List<BoatLockType>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val isAutomated = answer.contains(AUTOMATED)
        tags["automated"] = isAutomated.toYesNo()

        val isSelfService = answer.contains(SELF_SERVICE)
        tags["self_service"] = isSelfService.toYesNo()

        val isManual = answer.contains(MANUAL)
        tags["manual"] = isManual.toYesNo()

        // val hasSelfService = answer.contains(SELF_SERVICE)
        // val selfService = when {
        //     hasSelfService && answer.size == 1 -> "only"
        //     hasSelfService -> "yes"
        //     else -> "no"
        // }
        // tags["self_service"] = selfService
    }
}
