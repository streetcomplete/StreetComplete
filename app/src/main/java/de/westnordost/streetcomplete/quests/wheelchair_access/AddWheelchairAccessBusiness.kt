package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.isKindOfShopExpression
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.quests.place_name.getPlaceElementFilterString
import java.util.concurrent.FutureTask

class AddWheelchairAccessBusiness(
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
) : OsmFilterQuestType<WheelchairAccess>()
{
    override val elementFilter = """
        nodes, ways, relations with
          (name or brand)
          and access !~ no|private
          and !wheelchair
          and (
            shop and shop !~ no|vacant
            or amenity = parking and parking = multi-storey
            or amenity = recycling and recycling_type = centre
            or tourism = information and information = office
            or ${getPlaceElementFilterString(this)}
          )
    """

    override val changesetComment = "Add wheelchair access"
    override val wikiLink = "Key:wheelchair"
    override val icon = R.drawable.ic_quest_wheelchair_shop
    override val isReplaceShopEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override val questTypeAchievements = listOf(WHEELCHAIR)

    override fun getTitle(tags: Map<String, String>) =
        if (hasFeatureName(tags))
            R.string.quest_wheelchairAccess_name_type_title
        else
            R.string.quest_wheelchairAccess_name_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"]
        return arrayOfNotNull(name, featureName.value)
    }

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways, relations with " + isKindOfShopExpression())

    override fun createForm() = AddWheelchairAccessBusinessForm()

    override fun applyAnswerTo(answer: WheelchairAccess, changes: StringMapChangesBuilder) {
        changes.add("wheelchair", answer.osmValue)
    }

    private fun hasFeatureName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()
}
