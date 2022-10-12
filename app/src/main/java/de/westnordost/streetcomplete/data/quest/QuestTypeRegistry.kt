package de.westnordost.streetcomplete.data.quest

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
    private val trafficFlowSegmentsApi: TrafficFlowSegmentsApi? = null,
    private val trafficFlowDao: WayTrafficFlowDao? = null,
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>? = null,
    private val countryInfos: CountryInfos? = null,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>? = null,
    private val arSupportChecker: ArSupportChecker? = null,
    private val osmoseDao: OsmoseDao? = null,
    private val externalList: ExternalList? = null,
    private val quests: MutableList<QuestType> = mutableListOf()
) : List<QuestType> by quests {

    // the nullable stuff is just to allow creating a QuestTypeRegistry from a list as it's done in tests
    constructor(questList: List<QuestType>) : this(quests = questList.toMutableList()) {
        for (questType in this) {
            val questTypeName = questType.name
            require(!typeMap.containsKey(questTypeName)) {
                "A quest type's name must be unique! \"$questTypeName\" is defined twice!"
            }
            typeMap[questTypeName] = questType
        }
    }

    private val typeMap = mutableMapOf<String, QuestType>()

    init { reload() }

    fun reload() {
        if (trafficFlowSegmentsApi == null || trafficFlowDao == null || featureDictionaryFuture == null
            || countryInfos == null || countryBoundariesFuture == null || arSupportChecker == null
            || osmoseDao == null || externalList == null)
            return
        quests.clear()
        quests.addAll(getQuestTypeList(
            trafficFlowSegmentsApi,
            trafficFlowDao,
            featureDictionaryFuture,
            countryInfos,
            countryBoundariesFuture,
            arSupportChecker,
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
