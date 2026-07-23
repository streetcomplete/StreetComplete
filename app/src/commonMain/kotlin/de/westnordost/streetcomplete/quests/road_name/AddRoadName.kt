package de.westnordost.streetcomplete.quests.road_name

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.util.countryboundaries.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.osm.localized_name.applyTo
import de.westnordost.streetcomplete.resources.*

class AddRoadName : OsmFilterQuestType<List<LocalizedName>>() {

    override val elementFilter = """
        ways with
          highway ~ primary|secondary|tertiary|unclassified|residential|living_street|pedestrian|busway
          and (
             !name and !name:left and !name:right
             or ~fixme|FIXME ~ name|name\?|Name|Name\?
          )
          and !ref
          and noname != yes
          and name:signed != no
          and !junction
          and area != yes
          and (
            access !~ private|no
            or foot and foot !~ private|no
          )
    """
    override val enabledInCountries = AllCountriesExcept("JP")
    override val changesetComment = "Determine road names and types"
    override val wikiLink = "Key:name"
    override val icon = Res.drawable.quest_street_name2
    override val title = Res.string.quest_streetName_title
    override val hasMarkersAtEnds = true
    override val achievements = listOf(CAR, PEDESTRIAN, POSTMAN)
    override val hint = Res.string.quest_streetName_hint

    @Composable
    override fun Form(on: (QuestAction<List<LocalizedName>>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddRoadNameForm(on, element, countryInfo)
    }

    override fun applyAnswerTo(answer: List<LocalizedName>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer.isEmpty()) {
            tags["noname"] = "yes"
        } else {
            val singleName = answer.singleOrNull()
            if (singleName?.isRef() == true) {
                tags["ref"] = singleName.name
            } else {
                answer.applyTo(tags)
            }
        }
    }
}

private fun LocalizedName.isRef() =
    languageTag.isEmpty() && name.matches("[A-Z]{0,3}[ -]?[0-9]{0,5}".toRegex())
