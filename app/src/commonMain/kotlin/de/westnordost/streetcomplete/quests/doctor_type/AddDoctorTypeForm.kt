package de.westnordost.streetcomplete.quests.doctor_type

import androidx.compose.runtime.Composable
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.ui.common.quest.FeatureSelectionForm
import de.westnordost.streetcomplete.util.ktx.geometryType
import org.koin.compose.koinInject

val POPULAR_DOCTORS_FEATURE_IDS = listOf(
    // ordered roughly by usage number according to taginfo
    "amenity/doctors/chiropractic",
    "amenity/doctors/ophthalmology",
    "amenity/doctors/paediatrics",
)

@Composable
fun AddDoctorTypeForm(
    on: (QuestAction<Feature>) -> Unit,
    element: Element,
    featureDictionary: FeatureDictionary = koinInject()
) {
    FeatureSelectionForm(
        on = on,
        featureDictionary = featureDictionary,
        geometryType = element.geometryType,
        filterFn = { (it.tags["amenity"] == "doctors" || it.tags["amenity"]=="dentist") },
        codesOfDefaultFeatures = POPULAR_DOCTORS_FEATURE_IDS
    )
}
