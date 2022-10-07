package de.westnordost.streetcomplete.data.quest

import android.content.SharedPreferences
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.quests.external.ExternalList
import de.westnordost.streetcomplete.quests.getQuestTypeList
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegmentsApi
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.quests.osmose.OsmoseDao
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import java.util.concurrent.FutureTask

/** Every osm quest needs to be registered here.
 *
 * Could theoretically be done with Reflection, but that doesn't really work on Android
 */

class QuestTypeRegistry(
    private val trafficFlowSegmentsApi: TrafficFlowSegmentsApi,
    private val trafficFlowDao: WayTrafficFlowDao,
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>,
    private val countryInfos: CountryInfos,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
    private val arSupportChecker: ArSupportChecker,
    private val prefs: SharedPreferences,
    private val osmoseDao: OsmoseDao,
    private val externalList: ExternalList,
    private val quests: MutableList<QuestType> = mutableListOf()
) : List<QuestType> by quests {

    private val typeMap = mutableMapOf<String, QuestType>()

    init { reload() }

    fun reload() {
        quests.clear()
        quests.addAll(getQuestTypeList(
            trafficFlowSegmentsApi,
            trafficFlowDao,
            featureDictionaryFuture,
            countryInfos,
            countryBoundariesFuture,
            arSupportChecker,
            prefs,
            osmoseDao,
            externalList,
        ))
        typeMap.clear()
        for (questType in this) {
            val questTypeName = questType.name
            require(!typeMap.containsKey(questTypeName)) {
                "A quest type's name must be unique! \"$questTypeName\" is defined twice!"
            }
            typeMap[questTypeName] = questType
        }
    }

    fun getByName(typeName: String): QuestType? {
        return typeMap[typeName]
    }
}
