package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.ObjectTypeRegistry

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.getByLocation
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.quests.custom.CustomQuestList
import de.westnordost.streetcomplete.quests.getQuestTypeList
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegmentsApi
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.quests.osmose.OsmoseDao
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.util.ktx.getFeature
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

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
    private val featureDictionary: Lazy<FeatureDictionary> by inject(named("FeatureDictionaryLazy"))
    private val countryInfos: CountryInfos by inject()
    private val countryBoundaries: Lazy<CountryBoundaries> by inject(named("CountryBoundariesLazy"))
    private val arSupportChecker: ArSupportChecker by inject()
    private val getFeature: (Element) -> Feature? = { featureDictionary.value.getFeature(it) }
    private val getCountryInfoByLocation:  (location: LatLon) -> CountryInfo = { location ->
        countryInfos.getByLocation(countryBoundaries.value, location.longitude, location.latitude)
    }
    private val osmoseDao: OsmoseDao by inject()
    private val customQuestList: CustomQuestList by inject()

    fun reload() {
        ordinalsAndEntries.clear()
        ordinalsAndEntries.addAll(getQuestTypeList(
            trafficFlowSegmentsApi,
            trafficFlowDao,
            arSupportChecker,
            getCountryInfoByLocation,
            getFeature,
            osmoseDao,
            customQuestList,
        ))
        byName.clear()
        byOrdinal.clear()
        ordinalByObject.clear()
        objects.clear()
        reloadInit()
    }
}
