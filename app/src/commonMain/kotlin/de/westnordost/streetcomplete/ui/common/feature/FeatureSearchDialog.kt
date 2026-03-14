package de.westnordost.streetcomplete.ui.common.feature

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialogLayout

/** A search field and a list of results for features below, in a dialog */
@Composable
fun FeatureSearchDialog(
    onDismissRequest: () -> Unit,
    onSelectedFeature: (Feature) -> Unit,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
    geometryType: GeometryType? = null,
    countryCode: String? = null,
    filterFn: (Feature) -> Boolean = { true },
    codesOfDefaultFeatures: List<String> = emptyList(),
) {
    Dialog(onDismissRequest = onDismissRequest) {
        AlertDialogLayout(
            modifier = modifier,
            content = {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                    FeatureSearch(
                        onSelectedFeature = {
                            onDismissRequest()
                            onSelectedFeature(it)
                        },
                        featureDictionary = featureDictionary,
                        modifier = Modifier.fillMaxHeight(),
                        geometryType = geometryType,
                        countryCode = countryCode,
                        filterFn = filterFn,
                        codesOfDefaultFeatures = codesOfDefaultFeatures,
                    )
                }
            },
        )
    }
}
