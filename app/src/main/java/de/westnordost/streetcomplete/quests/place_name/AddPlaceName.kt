package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.isKindOfShopExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.quests.place_name.PlaceFilterQuestType.PLACE_NAME_QUEST
import java.util.concurrent.FutureTask

class AddPlaceName(
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
) : OsmElementQuestType<PlaceNameAnswer> {
    private val filter by lazy { """
        nodes, ways, relations with
        (
          shop and shop !~ no|vacant
          or craft
          or office
          or tourism = information and information = office
          or landuse ~ cemetery|allotments
          or military ~ airfield|barracks|training_area
          or ${getPlaceElementFilterString(PLACE_NAME_QUEST)}
        )
        and !name and !brand and noname != yes and name:signed != no
    """.toElementFilterExpression() }

    override val changesetComment = "Determine place names"
    override val wikiLink = "Key:name"
    override val icon = R.drawable.ic_quest_label
    override val isReplaceShopEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_placeName_title_name

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>) =
        arrayOfNotNull(featureName.value)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) && hasFeatureName(element.tags)

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways, relations with " + isKindOfShopExpression())

    override fun createForm() = AddPlaceNameForm()

    override fun applyAnswerTo(answer: PlaceNameAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is NoPlaceNameSign -> {
                changes.add("name:signed", "no")
            }
            is PlaceName -> {
                for ((languageTag, name) in answer.localizedNames) {
                    val key = when (languageTag) {
                        "" -> "name"
                        "international" -> "int_name"
                        else -> "name:$languageTag"
                    }
                    changes.addOrModify(key, name)
                }
            }
        }
    }

    private fun hasFeatureName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).find().isNotEmpty()
}
