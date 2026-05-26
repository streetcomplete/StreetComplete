package de.westnordost.streetcomplete.quests.diet_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.VEG
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*

class AddVegetarian : OsmFilterQuestType<DietAvailabilityAnswer>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity ~ restaurant|cafe|fast_food|food_court and food != no
          or amenity ~ pub|nightclub|biergarten|bar and food = yes
          or tourism ~ alpine_hut and food != no
        )
        and diet:vegan != only and (
          !diet:vegetarian
          or diet:vegetarian != only and diet:vegetarian older today -4 years
        )
    """
    override val changesetComment = "Survey whether places have vegetarian food"
    override val wikiLink = "Key:diet"
    override val icon = R.drawable.quest_restaurant_vegetarian
    override val title = Res.string.quest_dietType_vegetarian_title2
    override val achievements = listOf(VEG, CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside
    override val hint = Res.string.quest_dietType_explanation_vegetarian

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    @Composable
    override fun Form(onAnswer: (DietAvailabilityAnswer) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddDietTypeForm(onAnswer, element)
    }

    override fun applyAnswerTo(answer: DietAvailabilityAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is DietAvailability -> {
                tags.updateWithCheckDate("diet:vegetarian", answer.osmValue)
                if (answer.osmValue == "no") {
                    tags.remove("diet:vegan")
                }
            }
            NoFood -> tags["food"] = "no"
        }
    }
}
