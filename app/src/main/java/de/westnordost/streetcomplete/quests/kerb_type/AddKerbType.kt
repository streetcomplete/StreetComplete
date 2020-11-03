package de.westnordost.streetcomplete.quests.kerb_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType

class AddKerbType : OsmFilterQuestType<String>() {

    // TODO sst: adapt query or use different super class
    override val elementFilter = """
        ways with highway ~ path|footway|cycleway|bridleway|steps
        and segregated != yes
        and access !~ private|no
        and (!conveying or conveying = no)
        and (!indoor or indoor = no)
        and (
          !surface
          or surface ~ ${ANYTHING_UNPAVED.joinToString("|")} and surface older today -4 years
          or surface older today -8 years
          or (
            surface ~ paved|unpaved
            and !surface:note
            and !note:surface
          )
        )
    """
//    override val elementFilter = """
//        nodes with highway = crossing
//          and foot != no
//          and (
//            !crossing
//            or crossing ~ island|unknown|yes
//            or (
//              crossing ~ traffic_signals|uncontrolled|zebra|marked|unmarked
//              and crossing older today -8 years
//            )
//          )
//    """

    override val commitMessage = "Add kerb"
    override val wikiLink = "Key:kerb"
    override val icon = R.drawable.ic_quest_kerb
    override val isSplitWayEnabled = false

    override fun getTitle(tags: Map<String, String>) =  R.string.quest_kerb_type_title

    override fun createForm(): AddKerbTypeForm = AddKerbTypeForm() // TODO sst: change form

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("kerb", answer)
    }
}
