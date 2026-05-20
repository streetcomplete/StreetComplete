package de.westnordost.streetcomplete.overlays.things

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.POPULAR_THING_FEATURE_IDS
import de.westnordost.streetcomplete.osm.isThing
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.ui.common.feature.FeatureIcon
import de.westnordost.streetcomplete.ui.common.feature.FeatureItem
import de.westnordost.streetcomplete.ui.common.feature.FeatureSelect
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.util.ktx.geometryType

/** Form that shows and lets you select one thing, with a row of last picked things */
@Composable
fun ThingForm(
    selectedFeature: Feature?,
    onSelectedFeature: (Feature) -> Unit,
    lastPickedFeatures: List<Feature>,
    element: Element?,
    countryCode: String,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 96.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        if (isEnabled) {
            FeatureSelect(
                feature = selectedFeature,
                onSelectedFeature = onSelectedFeature,
                featureDictionary = featureDictionary,
                geometryType = element?.geometryType ?: GeometryType.POINT, // for new features: always POINT
                countryCode = countryCode,
                filterFn = { it.toElement().isThing() },
                codesOfDefaultFeatures = POPULAR_THING_FEATURE_IDS,
            )
            if (lastPickedFeatures.isNotEmpty()) {
                LastPickedChipsRow(
                    items = lastPickedFeatures,
                    onClick = onSelectedFeature,
                    modifier = Modifier.padding(start = 48.dp, end = 56.dp),
                    itemContent = {
                        FeatureIcon(
                            feature = it,
                            modifier = Modifier.size(22.5.dp)
                        )
                    }
                )
            } else {
                Spacer(Modifier.size(48.dp))
            }
        } else {
            selectedFeature?.let { item ->
                FeatureItem(
                    feature = item,
                    featureDictionary = featureDictionary,
                    countryCode = countryCode,
                )
            }
        }
    }
}
