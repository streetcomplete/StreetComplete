package de.westnordost.streetcomplete.quests.fuel_service

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddFuelSelfService : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
          amenity = fuel
          and !self_service
          and !automated
    """
    override val changesetComment = "Survey whether fuel stations provide self-service"
    override val wikiLink = "Key:self_service"
    override val icon = R.drawable.ic_quest_fuel_self_service
    override val achievements = listOf(CAR)
    override val enabledInCountries = NoCountriesExcept("IT", "UK")

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fuelSelfService_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["self_service"] = answer.toYesNo()
    }
}
