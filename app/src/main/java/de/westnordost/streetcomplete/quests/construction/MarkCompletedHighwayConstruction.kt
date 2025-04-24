package de.westnordost.streetcomplete.quests.construction

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.toCheckDateString
import de.westnordost.streetcomplete.osm.updateCheckDate

class MarkCompletedHighwayConstruction : OsmFilterQuestType<CompletedConstructionAnswer>() {

    override val elementFilter = """
        ways with
          (highway = construction or highway and construction = minor)
          and (!opening_date or opening_date < today)
          and older today -2 weeks
    """
    override val changesetComment = "Determine whether road construction is now completed"
    override val wikiLink = "Tag:highway=construction"
    override val icon = R.drawable.ic_quest_road_construction
    override val hasMarkersAtEnds = true
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) =
        when (tags["construction"]) {
            "minor" -> R.string.quest_construction_minor_title
            /*
              Alternative could be "Is this construction finished?" and just display the feature
              name (e.g. "cycleway under construction") of the element above, but there are no iD
              presets for "highway=construction + construction=*" so such road would just be named
              "Road closed". Hence, keeping this (for now).
             */
            in ALL_ROADS -> R.string.quest_construction_road_title
            "cycleway" -> R.string.quest_construction_cycleway_title
            "footway" -> R.string.quest_construction_footway_title
            "steps" -> R.string.quest_construction_steps_title
            else -> R.string.quest_construction_generic_title
        }

    override fun createForm() = MarkCompletedConstructionForm()

    override fun applyAnswerTo(answer: CompletedConstructionAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is OpeningDateAnswer -> {
                tags["opening_date"] = answer.date.toCheckDateString()
            }
            is StateAnswer -> {
                if (answer.value) {
                    // construction = minor has a special meaning: it is tagged *additionally to*
                    // the normal highway tag, because it denotes just minor construction works,
                    // i.e. road is not closed (completely) while the roadwork is done
                    if (tags["construction"] != "minor") {
                        tags["highway"] = tags["construction"] ?: "road"
                    }
                    removeTagsDescribingConstruction(tags)
                } else {
                    tags.updateCheckDate()
                }
            }
        }
    }
}
