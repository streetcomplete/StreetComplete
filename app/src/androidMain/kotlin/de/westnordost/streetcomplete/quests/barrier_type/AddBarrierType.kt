package de.westnordost.streetcomplete.quests.barrier_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddBarrierType : OsmFilterQuestType<BarrierType>() {

    override val elementFilter = """
        nodes with barrier = yes
         and !man_made
         and !historic
         and !military
         and !power
         and !tourism
         and !attraction
         and !amenity
         and !leisure
         and !aeroway
         and !railway
         and !craft
         and !healthcare
         and !office
         and !shop
    """
    override val changesetComment = "Specify type of barriers"
    override val wikiLink = "Key:barrier"
    override val icon = R.drawable.quest_barrier
    override val title = Res.string.quest_barrier_type_title
    override val achievements = listOf(CAR, PEDESTRIAN, BLIND, WHEELCHAIR, BICYCLIST, OUTDOORS)

    @Composable
    override fun Form(onAnswer: (BarrierType) -> Unit, element: Element) {
        AddBarrierTypeForm(onAnswer)
    }

    override fun applyAnswerTo(answer: BarrierType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) =
        answer.applyTo(tags)
}
