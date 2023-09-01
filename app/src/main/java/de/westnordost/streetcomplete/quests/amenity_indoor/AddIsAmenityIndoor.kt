package de.westnordost.streetcomplete.quests.amenity_indoor

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.*
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import java.util.concurrent.FutureTask

class AddIsAmenityIndoor (private val featureDictionaryFuture: FutureTask<FeatureDictionary>)
    : OsmElementQuestType<Boolean> {

    private val nodesFilter by lazy { """
        nodes with
         (
         emergency = defibrillator
         or emergency = fire_extinguisher
         or amenity = atm
         or amenity = telephone
         or amenity = parcel_locker
         or amenity = luggage_locker
         or amenity = locker
         or amenity = clock
         or amenity = post_box
         or amenity = public_bookcase
         or amenity = give_box
         or amenity = vending_machine and vending ~ parking_tickets|public_transport_tickets
         or amenity = ticket_validator
         )
         and access !~ private|no
         and !indoor and !location
    """.toElementFilterExpression() }

    /*
    *
    * */
    override val changesetComment = "Determine whether various amenitys are inside buildings"
    override val wikiLink = "Key:indoor"
    //TODO: Generisches Item
    override val icon = R.drawable.ic_quest_defibrillator
    //TODO: Noch schauen, wie man die achievements abhängig vom Element machen kann. AED -> LIFESAVER, parcel_locker->POSTMAN,etc.
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_is_amenity_inside_title
    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element) =
        nodesFilter.matches(element) && hasAnyName(element.tags)

    private fun hasAnyName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> {
        /* put markers for objects that are exactly the same as for which this quest is asking for
           e.g. it's a ticket validator? -> display other ticket validators. Etc. */
        val feature = featureDictionaryFuture.get()
            .byTags(element.tags)
            .isSuggestion(false) // not brands
            .find()
            .firstOrNull() ?: return emptySequence()

        return getMapData().filter { it.tags.containsAll(feature.tags) }.asSequence()
    }

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["indoor"] = answer.toYesNo()
        }
    }
private fun <X, Y> Map<X, Y>.containsAll(other: Map<X, Y>) = other.all { this[it.key] == it.value }


