package de.westnordost.streetcomplete.quests.postbox_collection_times

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddPostboxCollectionTimes(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<CollectionTimesAnswer>(o) {

    override val tagFilters = """
        nodes with amenity = post_box
        and access !~ private|no
        and collection_times:signed != no
        and (!collection_times or collection_times older today -${r * 2} years)
    """

    /* Don't ask again for postboxes without signed collection times. This is very unlikely to
    *  change and problematic to tag clearly with the check date scheme */

    override val icon = R.drawable.ic_quest_mail
    override val commitMessage = "Add postbox collection times"
    override val wikiLink = "Key:collection_times"

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

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"] ?: tags["operator"]
        return if (name != null) arrayOf(name) else arrayOf()
    }

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.keys.containsAny(listOf("name","brand","operator"))
        return if (hasName) R.string.quest_postboxCollectionTimes_name_title
               else         R.string.quest_postboxCollectionTimes_title
    }

    override fun createForm() = AddCollectionTimesForm()

    override fun applyAnswerTo(answer: CollectionTimesAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is NoCollectionTimesSign -> {
                changes.add("collection_times:signed", "no")
            }
            is CollectionTimes -> {
                changes.updateWithCheckDate("collection_times", answer.times.joinToString(", "))
            }
        }
    }
}
