package de.westnordost.streetcomplete.quests.handrail

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

/**
 * Only ask about whether there exists a handrail, not about the location (left, right and/or
 * middle), because the primary use case here is accessibility of the path (not 3d rendering, etc),
 * which does not depend on the location of the handrail.
 * The details would only slow down the answering in a survey by requiring more thought and a more
 * complex GUI (interpreting the direction of the OSM way for example).
 */
class AddHandrail(overpassServer: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<Boolean>(overpassServer) {
    // Do not include nodes and relations, even though these exist with the right tags, because
    // according to the wiki page for `highway=steps` [1] it can only be applied to ways. It also
    // does not make much sense for other types of elements and it is more likely to be a tagging
    // mistake.
    // Exclude elements tagged with `handrail:left`, `handrail:center` or `handrail:right`
    // even though according to the wiki page for `handrail=*` [1], the `handrail` tag should
    // always have just `yes` or `no` as a value to indicate the existence of a handrail somewhere.
    // If one of these tags is present but without a `handrail` tag, we still assume the survey data
    // itself was valid, so we do not need to use survey time to add the implied `handrail` tag.
    // If there is such a case, mappers can fix this from remote locations, maybe with the help of
    // one of the various Quality Assurance tools [3].
    // [1] https://wiki.openstreetmap.org/wiki/Tag:highway%3Dsteps
    // [2] https://wiki.openstreetmap.org/wiki/Key:handrail
    // [3] https://wiki.openstreetmap.org/wiki/Quality_assurance
    override val tagFilters = """
        ways with highway = steps
         and (!conveying or conveying = no)
         and !handrail and !handrail:left and !handrail:center and !handrail:right
    """
    override val commitMessage = "Add whether steps have a handrail"

    override val icon = R.drawable.ic_quest_handrail

    override fun getTitle(tags: Map<String, String>) = R.string.quest_handrail_title

    // use simple yes/no user interface without additional pictures for prompting user for answer
    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("handrail", if (answer) "yes" else "no")
    }
}
