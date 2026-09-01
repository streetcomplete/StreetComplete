package de.westnordost.streetcomplete.quests.doctor_type

import androidx.compose.runtime.Composable
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.ui.common.quest.FeaturesSelectionQuestForm
import de.westnordost.streetcomplete.util.ktx.geometryType
import org.koin.compose.koinInject

val POPULAR_DOCTORS_FEATURE_IDS = listOf(
    // ordered roughly by usage number according to taginfo (2026-09)
    // ~20%
    "amenity/doctors/general",
    // ~3%
    "amenity/doctors/gynaecology",
    "amenity/doctors/paediatrics",
    "amenity/doctors/internal",
    // ~2%
    "amenity/doctors/orthopaedics",
    "amenity/doctors/dermatology",
    "amenity/doctors/ophthalmology",
    "amenity/doctors/otolaryngology",
)

@Composable
fun AddDoctorTypeForm(
    on: (QuestAction<List<Feature>>) -> Unit,
    element: Element,
    featureDictionary: FeatureDictionary = koinInject()
) {
    FeaturesSelectionQuestForm(
        on = on,
        featureDictionary = featureDictionary,
        geometryType = element.geometryType,
        filterFn = { (it.tags["amenity"] == "doctors" && it.tags["healthcare:speciality"] != null) },
        codesOfDefaultFeatures = POPULAR_DOCTORS_FEATURE_IDS
    )
}
