package de.westnordost.streetcomplete.quests.handrail

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

/**
 * Only ask about whether there exists a handrail, not about the location (left, right and/or
 * middle), because the primary use case here is accessibility of the path (not 3d rendering, etc),
 * which does not depend on the location of the handrail.
 * The details would only slow down the answering in a survey by requiring more thought and a more
 * complex GUI (interpreting the direction of the OSM way for example).
 *
 * Github Issue for this quest:
 * https://github.com/westnordost/StreetComplete/issues/1390
 */
class AddHandrail(overpassServer: OverpassMapDataDao) : SimpleOverpassQuestType<Boolean>(overpassServer) {
    // Do not include nodes and relations, even though these exist with the right tags, because
    // according to the wiki page for `highway=steps` [2] it can only be applied to ways. It also
    // does not make much sense for other types of elements and it is more likely to be a
    // Do not exclude elements tagged with `handrail:left`, `handrail:center` or `handrail:right`
    // because according to the wiki page for `handrail=*` [1], the `handrail` tag should
    // always have just `yes` or
    // `no` as a value to indicate the existence of a handrail somewhere. If there is a mismatch of
    // the tags afterwards, that can be recognized using one of the various
    // Quality Assurance tools [3].
    // [1] https://wiki.openstreetmap.org/wiki/Key:handrail
    // [2] https://wiki.openstreetmap.org/wiki/Tag:highway%3Dsteps
    // [3] https://wiki.openstreetmap.org/wiki/Quality_assurance
    override val tagFilters = "nodes with highway = steps and !handrail"

    // changeset comment to be used for uploading changes made through this quest
    override val commitMessage = "Add whether steps have a handrail"

    // icon for displaying quest on the map
    override val icon = R.drawable.ic_quest_handrail

    override fun getTitle(tags: Map<String, String>) = R.string.quest_handrail_title

    // use simple yes/no user interface without additional pictures for prompting user for answer
    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answerHandrailExists: Boolean, changes: StringMapChangesBuilder) {
        changes.add("handrail", if (answerHandrailExists) "yes" else "no")
    }
}
