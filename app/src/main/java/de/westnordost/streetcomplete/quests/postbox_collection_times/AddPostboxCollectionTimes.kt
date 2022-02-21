package de.westnordost.streetcomplete.quests.postbox_collection_times

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.ktx.containsAnyKey
import de.westnordost.streetcomplete.osm.opening_hours.parser.isSupportedCollectionTimes
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHoursRules
import de.westnordost.streetcomplete.quests.getNameOrBrandOrOperatorOrRef

class AddPostboxCollectionTimes : OsmElementQuestType<CollectionTimesAnswer> {

    private val filter by lazy { """
        nodes with amenity = post_box
          and access !~ private|no
          and collection_times:signed != no
          and (!collection_times or collection_times older today -2 years)
    """.toElementFilterExpression() }

    /* Don't ask again for postboxes without signed collection times. This is very unlikely to
    *  change and problematic to tag clearly with the check date scheme */

    override val changesetComment = "Add postbox collection times"
    override val wikiLink = "Key:collection_times"
    override val icon = R.drawable.ic_quest_mail
    override val isDeleteElementEnabled = true

    // See overview here: https://ent8r.github.io/blacklistr/?streetcomplete=postbox_collection_times/AddPostboxCollectionTimes.kt
    // sources:
    // https://www.itinerantspirit.com/home/2016/5/22/post-boxes-from-around-the-world
    // https://commons.wikimedia.org/wiki/Category:Post_boxes_by_country
    // http://wanderlustexplorers.com/youve-got-mail-23-international-postal-boxes/
    override val enabledInCountries = NoCountriesExcept(
        // definitely, seen pictures:
        "AU", "NZ", "VU", "MY", "SG", "TH", "VN", "LA", "MM", "IN", "BD", "NP", "LK", "BT", "PK", "TW", "HK",
        "MO", "CN", "KR", "JP", "RU", "BY", "LT", "LV", "FI", "SE", "NO", "DK", "GB", "IE", "IS", "NL", "BE",
        "FR", "AD", "ES", "PT", "CH", "LI", "AT", "DE", "LU", "MC", "IT", "SM", "MT", "PL", "EE", "CA", "US",
        "UA", "SK", "CZ", "HU", "RO", "MD", "BG", "SI", "HR", "IL", "ZA", "GR", "UZ", "ME", "CY", "TR", "LB",
        // these only maybe/sometimes (Oceania, Cambodia, North Korea):
        "BN", "KH", "ID", "TL", "PG", "KP", "PH",
        // unknown but all countries around have it (former Yugoslawia):
        "RS", "RS-KM", "BA", "MK", "AL",
        // unknown but region around it has it (southern states of former soviet union):
        "TJ", "KG", "KZ", "MN", "GE"
        // apparently mostly not in Latin America and in Arabic world and unknown in Africa
    )

    override val questTypeAchievements = listOf(POSTMAN)

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> =
        arrayOfNotNull(getNameOrBrandOrOperatorOrRef(tags))

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsAnyKey("name", "brand", "operator", "ref")
        /* treat invalid collection times like it is not set at all. Any opening hours are
           legal tagging for collection times, even though they are not supported in
           this app, i.e. are never asked again */
        val hasValidCollectionTimes = tags["collection_times"]?.toOpeningHoursRules() != null
        return if (hasValidCollectionTimes) {
            when {
                hasName -> R.string.quest_postboxCollectionTimes_resurvey_name_title
                else    -> R.string.quest_postboxCollectionTimes_resurvey_title
            }
        } else {
            when {
                hasName -> R.string.quest_postboxCollectionTimes_name_title
                else    -> R.string.quest_postboxCollectionTimes_title
            }
        }
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean {
        if (!filter.matches(element)) return false
        val tags = element.tags
        // no collection_times yet -> new survey
        val ct = tags["collection_times"] ?: return true
        // invalid opening_hours rules -> applicable because we want to ask for opening hours again
        val rules = ct.toOpeningHoursRules() ?: return true
        // only display supported rules
        return rules.isSupportedCollectionTimes()
    }

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = post_box")

    override fun createForm() = AddPostboxCollectionTimesForm()

    override fun applyAnswerTo(answer: CollectionTimesAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is NoCollectionTimesSign -> {
                tags["collection_times:signed"] = "no"
            }
            is CollectionTimes -> {
                tags.updateWithCheckDate("collection_times", answer.times.toString())
            }
        }
    }
}
