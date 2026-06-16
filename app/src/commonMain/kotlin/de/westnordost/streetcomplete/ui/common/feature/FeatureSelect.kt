package de.westnordost.streetcomplete.ui.common.feature

import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.ui.ItemCard

/** Shows the currently selected feature and lets user change it */
@Composable
fun FeatureSelect(
    feature: Feature?,
    onSelectedFeature: (Feature) -> Unit,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
    geometryType: GeometryType? = null,
    countryCode: String? = null,
    filterFn: (Feature) -> Boolean = { true },
    codesOfDefaultFeatures: List<String> = emptyList(),
) {
    var showDialog by remember { mutableStateOf(false) }

    ItemCard(
        item = feature,
        expanded = showDialog,
        onExpandChange = { showDialog = it },
        modifier = modifier,
    ) { item ->
        CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)) {
            FeatureItem(
                feature = item,
                featureDictionary = featureDictionary,
                countryCode = countryCode,
            )
        }
    }
    if (showDialog) {
        FeatureSearchDialog(
            onDismissRequest = { showDialog = false },
            onSelectedFeature = onSelectedFeature,
            featureDictionary = featureDictionary,
            geometryType = geometryType,
            countryCode = countryCode,
            filterFn = filterFn,
            codesOfDefaultFeatures = codesOfDefaultFeatures,
        )
    }
}
