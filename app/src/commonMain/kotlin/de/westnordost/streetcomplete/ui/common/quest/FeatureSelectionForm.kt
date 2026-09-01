package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.feature.FeatureItem
import de.westnordost.streetcomplete.ui.common.feature.FeatureSearchDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * A quest form that allows selecting of features via search or from a list of presets.
 */
@Composable
fun FeaturesSelectionQuestForm(
    on: (QuestAction<List<Feature>>) -> Unit,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
    initialSelectedFeatures: List<Feature> = emptyList(),
    geometryType: GeometryType? = null,
    countryCode: String? = null,
    filterFn: (Feature) -> Boolean = { true },
    codesOfDefaultFeatures: List<String> = emptyList(),
) {
    var selectedFeatures by remember { mutableStateOf(initialSelectedFeatures) }
    var showSearch by remember  { mutableStateOf(false) }

    QuestForm(
        on = on,
        isComplete = selectedFeatures.isNotEmpty(),
        onClickOk = { on(Answer(selectedFeatures)) },
        modifier = modifier,
    ) {
        Column {
            selectedFeatures.forEach { feature ->
                FeatureRow(
                    feature = feature,
                    featureDictionary = featureDictionary,
                    countryCode = countryCode,
                    onRemove = { selectedFeatures = selectedFeatures - feature },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button2(
                onClick = { showSearch = true }
            ) {
                Icon(painterResource(Res.drawable.ic_add_24), contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(Res.string.add))
            }

            if (showSearch) {
                FeatureSearchDialog(
                    onDismissRequest = { showSearch = false },
                    onSelectedFeature = {
                        selectedFeatures = selectedFeatures + it
                        showSearch = false
                    },
                    featureDictionary = featureDictionary,
                    geometryType = geometryType,
                    countryCode = countryCode,
                    // Ensure a preset cannot be selected twice
                    filterFn = { feature -> feature !in selectedFeatures && filterFn(feature) },
                    codesOfDefaultFeatures = codesOfDefaultFeatures
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    feature: Feature,
    featureDictionary: FeatureDictionary,
    countryCode: String?,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FeatureItem(
            feature = feature,
            featureDictionary = featureDictionary,
            countryCode = countryCode,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(painterResource(Res.drawable.ic_delete_24), contentDescription = null)
        }
    }
}
