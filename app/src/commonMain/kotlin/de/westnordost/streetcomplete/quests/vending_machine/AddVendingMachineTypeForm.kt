package de.westnordost.streetcomplete.quests.vending_machine

import androidx.compose.runtime.Composable
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.ui.common.quest.FeatureSelectionForm
import de.westnordost.streetcomplete.util.ktx.geometryType
import org.koin.compose.koinInject

val POPULAR_VENDING_MACHINE_FEATURE_IDS = listOf(
    // ordered roughly by usage number according to taginfo
    "amenity/vending_machine/parking_tickets",
    "amenity/vending_machine/excrement_bags",
    "amenity/vending_machine/drinks",
    "amenity/vending_machine/public_transport_tickets",
    "amenity/vending_machine/cigarettes",
)

@Composable
fun AddVendingMachineTypeForm(
    on: (QuestAction<List<Feature>>) -> Unit,
    element: Element,
    featureDictionary: FeatureDictionary = koinInject()
) {
    FeatureSelectionForm(
        on = on,
        featureDictionary = featureDictionary,
        geometryType = element.geometryType,
        filterFn = { (it.tags["amenity"] == "vending_machine" && it.tags["vending"] != null) },
        codesOfDefaultFeatures = POPULAR_VENDING_MACHINE_FEATURE_IDS
    )
}
