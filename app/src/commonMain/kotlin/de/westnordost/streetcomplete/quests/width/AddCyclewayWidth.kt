package de.westnordost.streetcomplete.quests.width

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import org.jetbrains.compose.resources.StringResource

class AddCyclewayWidth(
    private val checkArSupport: ArSupportChecker
) : OsmFilterQuestType<WidthAnswer>() {

    /* All either exclusive cycleways or ways that are cycleway + footway (or bridleway) but
     *  segregated */
    override val elementFilter = """
        ways with (
          (
            highway = cycleway
            and foot !~ yes|designated
            and (!width or source:width ~ ".*estimat.*")
          ) or (
            segregated = yes
            and (
              highway = cycleway and foot ~ yes|designated
              or highway ~ path|footway and bicycle != no
              or highway = bridleway and bicycle ~ designated|yes
            )
            and (!cycleway:width or source:cycleway:width ~ ".*estimat.*")
          )
        )
        and area != yes
        and access !~ private|no
        and placement != transition
        and ~path|footway|cycleway|bridleway !~ link
    """
    override val changesetComment = "Specify cycleways width"
    override val wikiLink = "Key:width"
    override val icon = Res.drawable.quest_bicycleway_width
    override val title = Res.string.quest_cycleway_width_title
    override val achievements = listOf(BICYCLIST)
    override val defaultDisabledMessage: StringResource?
        get() = if (!checkArSupport()) Res.string.default_disabled_msg_no_ar else null

    @Composable
    override fun Form(onAnswer: (WidthAnswer) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddWidthForm(onAnswer, element, countryInfo)
    }

    override fun applyAnswerTo(answer: WidthAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val isExclusive = tags["highway"] == "cycleway" && tags["foot"] != "yes" && tags["foot"] != "designated"

        val key = if (isExclusive) "width" else "cycleway:width"

        tags[key] = answer.width.toOsmValue()
        if (answer.isARMeasurement) {
            tags["source:$key"] = "ARCore"
        } else {
            tags.remove("source:$key")
        }
    }
}
