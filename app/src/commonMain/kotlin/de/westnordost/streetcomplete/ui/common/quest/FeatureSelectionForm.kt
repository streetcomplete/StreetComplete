package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.ui.common.feature.FeatureSearch

/**
 * A quest form that allows selecting a feature via search or from a list of defaults.
 */
@Composable
fun FeatureSelectionForm(
    on: (QuestAction<Feature>) -> Unit,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
    geometryType: GeometryType? = null,
    countryCode: String? = null,
    filterFn: (Feature) -> Boolean = { true },
    codesOfDefaultFeatures: List<String> = emptyList(),
) {
    QuestForm(
        on = on,
        isComplete = false,
        onClickOk = { /* item selection is immediate */ },
        modifier = modifier,
    ) {
        FeatureSearch(
            onSelectedFeature = { on(Answer(it)) },
            featureDictionary = featureDictionary,
            modifier = Modifier.fillMaxWidth(),
            geometryType = geometryType,
            countryCode = countryCode,
            filterFn = filterFn,
            codesOfDefaultFeatures = codesOfDefaultFeatures,
            isScrollable = false
        )
    }
}
