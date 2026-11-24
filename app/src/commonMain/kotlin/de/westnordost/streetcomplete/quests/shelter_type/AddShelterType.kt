package de.westnordost.streetcomplete.quests.shelter_type

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_access
import de.westnordost.streetcomplete.resources.quest_shelter_type_title
import de.westnordost.streetcomplete.ui.common.quest.TreeNode
import de.westnordost.streetcomplete.ui.common.quest.TreeQuestForm

class AddShelterType : OsmFilterQuestType<ShelterType>() {

    override val elementFilter = """
        nodes, ways, relations with
         amenity=shelter
          and !shelter_type
          and access !~ private|customers
    """
    override val changesetComment = "Specify tower type"
    override val wikiLink = "Key:tower:type"
    override val icon = Res.drawable.quest_access
    override val title = Res.string.quest_shelter_type_title
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(on: (QuestAction<ShelterType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        TreeQuestForm(
            on = on,
            items = TreeNode(
                yes = TreeNode(ShelterType.PUBLIC_TRANSPORT),
                no = TreeNode(
                    question = "Can you sleep here?",
                    yes= TreeNode(
                        question = "Is it full enclosed?",
                        yes=TreeNode(ShelterType.BASIC_HUT),
                        no=TreeNode(ShelterType.LEAN_TO),
                    ),
                    no= TreeNode(
                        question= "Is it intended to protect from the elements?",
                        yes= TreeNode(
                            question = "Is it built to only protect from the sun?",
                            yes = TreeNode(ShelterType.SUN_SHELTER),
                            no = TreeNode(ShelterType.WEATHER_SHELTER),
                        ),
                        no = TreeNode(
                            question = "Is there a picknick table inside?",
                            yes= TreeNode(ShelterType.PICNIC_SHELTER),
                            no = TreeNode(
                                question= "Is it intended for animals?",
                                yes=TreeNode(ShelterType.FIELD_SHELTER),
                                no = TreeNode(
                                    question= "Is it for recreation?",
                                    yes=TreeNode(
                                        question= "Is it made of wood?",
                                        yes=TreeNode(ShelterType.GAZEBO),
                                        no = TreeNode(ShelterType.PAVILION),
                                    ),
                                ),
                            ),
                        )
                    ),
                ),
                question = "Is this part of a public transport stop?",
            ),
            itemContent = { Text(it.title, style = MaterialTheme.typography.subtitle1) },
        )
    }

    override fun applyAnswerTo(answer: ShelterType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["shelter_type"] = answer.osmValue
    }
}

