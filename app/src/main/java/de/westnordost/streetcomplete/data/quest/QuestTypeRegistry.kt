package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.ObjectTypeRegistry

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.quests.external.ExternalList
import de.westnordost.streetcomplete.quests.getQuestTypeList
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegmentsApi
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.quests.osmose.OsmoseDao
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

import java.util.concurrent.FutureTask

/** Every osm quest needs to be registered here.
 *
 * Could theoretically be done with Reflection, but that doesn't really work on Android.
 *
 * It is also used to define a (display) order of the quest types and to assign an ordinal to each
 * quest type for serialization.
 */
class QuestTypeRegistry(initialOrdinalsAndEntries: List<Pair<Int, QuestType>>, private val ordinalsAndEntries: MutableList<Pair<Int, QuestType>> = initialOrdinalsAndEntries.toMutableList()) : ObjectTypeRegistry<QuestType>(ordinalsAndEntries), KoinComponent {
    private val trafficFlowSegmentsApi: TrafficFlowSegmentsApi by inject()
    private val trafficFlowDao: WayTrafficFlowDao by inject()
    private val featureDictionaryFuture: FutureTask<FeatureDictionary> by inject(named("FeatureDictionaryFuture"))
    private val countryInfos: CountryInfos by inject()
    private val countryBoundariesFuture: FutureTask<CountryBoundaries> by inject(named("CountryBoundariesFuture"))
    private val arSupportChecker: ArSupportChecker by inject()
    private val osmoseDao: OsmoseDao by inject()
    private val externalList: ExternalList by inject()

    fun reload() {
        ordinalsAndEntries.clear()
        ordinalsAndEntries.addAll(getQuestTypeList(
            trafficFlowSegmentsApi,
            trafficFlowDao,
            featureDictionaryFuture,
            countryInfos,
            countryBoundariesFuture,
            arSupportChecker,
            osmoseDao,
            externalList,
        ))
        byName.clear()
        byOrdinal.clear()
        ordinalByObject.clear()
        objects.clear()
        reloadInit()
    }
}
