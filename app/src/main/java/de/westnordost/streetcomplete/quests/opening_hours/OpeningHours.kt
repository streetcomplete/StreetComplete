package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.ktx.containsAny
import java.util.concurrent.FutureTask

abstract class OpeningHours(private val featureDictionaryFuture: FutureTask<FeatureDictionary>) : OsmElementQuestType<OpeningHoursAnswer> {
    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"] ?: featureName.value
        return if (name != null) arrayOf(name) else arrayOf()
    }

    fun hasName(tags: Map<String, String>?) = hasProperName(tags) || hasFeatureName(tags)

    fun hasProperName(tags: Map<String, String>?): Boolean =
            tags?.keys?.containsAny(listOf("name", "brand")) ?: false

    fun hasFeatureName(tags: Map<String, String>?): Boolean =
            tags?.let { featureDictionaryFuture.get().byTags(it).find().isNotEmpty() } ?: false
}
