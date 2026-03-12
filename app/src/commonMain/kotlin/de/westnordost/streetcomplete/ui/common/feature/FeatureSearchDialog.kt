package de.westnordost.streetcomplete.ui.common.feature

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties(),
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
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
            shape = shape,
            backgroundColor = backgroundColor,
            contentColor = contentColor
        )
    }
}
