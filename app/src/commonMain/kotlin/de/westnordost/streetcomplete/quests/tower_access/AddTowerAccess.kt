package de.westnordost.streetcomplete.quests.tower_access

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddTowerAccess : OsmFilterQuestType<TowerAccess>() {

    override val elementFilter = """
        nodes, ways, relations with
            man_made = tower
            and (
                tower:type = observation
                or tower:type = watchtower and historic=yes
            )
            and disused != yes
            and !emergency
            and !military
            and (!access or access = unknown)
        """
    override val changesetComment = "Specify access to observation towers"
    override val wikiLink = "Tag:man_made=tower"
    override val icon = Res.drawable.quest_tower
    override val title = Res.string.quest_tower_access_title
    override val achievements = listOf(BUILDING)

    @Composable
    override fun Form(on: (QuestAction<TowerAccess>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        RadioGroupQuestForm(
            on = on,
            items = TowerAccess.entries,
            itemContent = { Text(stringResource(it.text)) },
        )
    }

    override fun applyAnswerTo(answer: TowerAccess, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["access"] = answer.osmValue
    }
}
