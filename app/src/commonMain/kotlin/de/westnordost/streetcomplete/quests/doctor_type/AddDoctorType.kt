package de.westnordost.streetcomplete.quests.doctor_type

import androidx.compose.runtime.Composable
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddDoctorType() : OsmFilterQuestType<List<Feature>>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity=doctors
          and !healthcare:speciality
        )
    """
    override val changesetComment = "Survey what type of healthcare a doctor provides"
    override val wikiLink = "Key:healthcare:speciality"
    override val icon = Res.drawable.quest_doctor_type
    override val title = Res.string.quest_doctor_type_title
    override val achievements = listOf(CITIZEN, LIFESAVER)

    @Composable
    override fun Form(on: (QuestAction<List<Feature>>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddDoctorTypeForm(on, element)
    }

    override fun applyAnswerTo(answer: List<Feature>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["healthcare:speciality"] = answer.joinToString(";") { it.tags["healthcare:speciality"] ?: "" }
    }
}
