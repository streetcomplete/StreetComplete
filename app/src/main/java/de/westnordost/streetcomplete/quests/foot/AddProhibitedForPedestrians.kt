package de.westnordost.streetcomplete.quests.foot

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.*

class AddProhibitedForPedestrians(o: OverpassMapDataDao) : SimpleOverpassQuestType<ProhibitedForPedestriansAnswer>(o) {

    override val tagFilters = """
        ways with !foot and (
          sidewalk ~ none|no or
          sidewalk:both ~ none|no or
          (sidewalk:left ~ none|no and sidewalk:right ~ none|no)
        )
        and access !~ private|no
        """ +
        /* asking for any road without sidewalk is too much. Main interesting situations are
           certain road sections within large intersections, overpasses, underpasses,
           inner segregated lanes of large streets, connecting/linking road way sections and so
           forth. See https://lists.openstreetmap.org/pipermail/tagging/2019-February/042852.html */
        // only roads where foot=X is not (almost) implied
        "and motorroad != yes " +
        "and highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified " +
        // road probably not developed enough to issue a prohibition for pedestrians
        "and surface ~ ${OsmTaggings.ANYTHING_PAVED.joinToString("|")} " +
        // fuzzy filter for above mentioned situations + developed-enough / non-rural roads
        "and ( oneway~yes|-1 or bridge=yes or tunnel=yes or bicycle~no|use_sidepath or lit=yes )"

    override val commitMessage = "Add whether roads are accessible for pedestrians"
    override val icon = R.drawable.ic_quest_pedestrian

    override fun getTitle(tags: Map<String, String>) = R.string.quest_accessible_for_pedestrians_title_prohibited

    override fun createForm() = AddAccessibleForPedestriansForm()

    override fun applyAnswerTo(answer: ProhibitedForPedestriansAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            // the question is whether it is prohibited, so YES -> foot=no etc
            YES -> changes.add("foot", "no")
            NO -> changes.add("foot", "yes")
            HAS_SEPARATE_SIDEWALK -> {
                changes.add("foot", "use_sidepath")
                changes.modify("sidewalk", "separate")
            }
            IS_LIVING_STREET -> changes.modify("highway", "living_street")
        }
    }
}
