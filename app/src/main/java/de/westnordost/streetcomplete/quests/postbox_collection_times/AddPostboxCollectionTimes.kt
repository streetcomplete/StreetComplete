package de.westnordost.streetcomplete.quests.postbox_collection_times

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.Countries
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddPostboxCollectionTimes(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = """
        nodes with amenity=post_box and !collection_times
        and collection_times:signed != no and access !~ private|no"
    """
    override val icon = R.drawable.ic_quest_mail
    override val commitMessage = "Add postbox collection times"

    // See overview here: https://ent8r.github.io/blacklistr/?java=postbox_collection_times/AddPostboxCollectionTimes.kt
    // sources:
    // https://www.itinerantspirit.com/home/2016/5/22/post-boxes-from-around-the-world
    // https://commons.wikimedia.org/wiki/Category:Post_boxes_by_country
    // http://wanderlustexplorers.com/youve-got-mail-23-international-postal-boxes/
    override val enabledForCountries:Countries = Countries.noneExcept(arrayOf(
        // definitely, seen pictures:
        "AU","NZ","VU","MY","SG","TH","VN","LA","MM","IN","BD","NP","LK","BT","PK","TW","HK",
        "MO","CN","KR","JP","RU","BY","LT","LV","FI","SE","NO","DK","GB","IE","IS","NL","BE",
        "FR","AD","ES","PT","CH","LI","AT","DE","LU","MC","IT","SM","MT","PL","EE","CA","US",
        "UA","SK","CZ","HU","RO","MD","BG","SI","HR","IL","ZA","GR","UZ","ME","CY","TR","LB",
        // these only maybe/sometimes (Oceania, Cambodia, North Korea):
        "BN","KH","ID","TL","PG","KP","PH",
        // unknown but all countries around have it (former Yugoslawia):
        "RS","RS-KM","BA","MK","AL",
        // unknown but region around it has it (southern states of former soviet union):
        "TJ","KG","KZ","MN","GE"
        // apparently mostly not in Latin America and in Arabic world and unknown in Africa
    ))

    override fun getTitle(tags: Map<String, String>) = R.string.quest_postboxCollectionTimes_title

    override fun createForm() = AddCollectionTimesForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val noTimes = answer.getBoolean(AddCollectionTimesForm.NO_TIMES_SPECIFIED)
        if (noTimes) {
            changes.add("collection_times:signed", "no")
        } else {
            changes.add("collection_times", answer.getString(AddCollectionTimesForm.TIMES)!!)
        }
    }
}
