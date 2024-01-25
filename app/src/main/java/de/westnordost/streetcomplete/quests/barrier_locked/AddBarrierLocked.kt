package de.westnordost.streetcomplete.quests.barrier_locked

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddBarrierLocked : OsmFilterQuestType<BarrierLockedAnswer>() {

    override val elementFilter = """
        nodes, ways with
          barrier ~ bump_gate|chain|door|gate|swing_gate|sliding_gate|sliding_beam|wicket_gate
        and (
          !locked
          or locked = yes and locked older today -5 years
          or locked older today -10 years
        )
    """
    override val changesetComment = "Add whether barriers are locked"
    override val wikiLink = "Key:locked"
    override val icon = R.drawable.ic_quest_barrier_locked
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_barrier_locked_title

    override fun createForm() = AddBarrierLockedForm()

    override fun applyAnswerTo(answer: BarrierLockedAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
