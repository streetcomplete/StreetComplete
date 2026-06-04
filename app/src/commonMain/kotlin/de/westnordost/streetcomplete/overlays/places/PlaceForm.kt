package de.westnordost.streetcomplete.overlays.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.places.POPULAR_PLACE_FEATURE_IDS
import de.westnordost.streetcomplete.osm.hasFixedName
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.feature.FeatureIcon
import de.westnordost.streetcomplete.ui.common.feature.FeatureSelect
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.common.localized_name.LocalizedNamesForm
import de.westnordost.streetcomplete.util.ktx.geometryType
import org.jetbrains.compose.resources.stringResource

/** Form that shows and lets you select one place, with a row of last picked places */
@Composable
fun PlaceForm(
    selectedFeature: Feature?,
    onSelectedFeature: (Feature) -> Unit,
    lastPickedFeatures: List<Feature>,
    localizedNames: List<LocalizedName>,
    isNoName: Boolean,
    selectableLanguages: List<String>,
    onLocalizedNamesChanged: (List<LocalizedName>) -> Unit,
    element: Element?,
    countryCode: String,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
) {
    val hasFixedName = remember(selectedFeature) { selectedFeature?.hasFixedName == true }

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 96.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        FeatureSelect(
            feature = selectedFeature,
            onSelectedFeature = onSelectedFeature,
            featureDictionary = featureDictionary,
            geometryType = element?.geometryType ?: GeometryType.POINT,
            countryCode = countryCode,
            filterFn = { it.toElement().isPlace() || it.id == "shop/vacant" },
            codesOfDefaultFeatures = POPULAR_PLACE_FEATURE_IDS,
        )
        if (selectedFeature != null && !hasFixedName) {
            Column {
                Text(
                    text = stringResource(Res.string.name_label),
                    style = MaterialTheme.typography.caption.copy(
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                    )
                )
                if (isNoName && localizedNames.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.quest_placeName_no_name_answer),
                        style = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                        ),
                        modifier = Modifier
                            .padding(20.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                LocalizedNamesForm(
                    localizedNames = localizedNames,
                    onChanged = onLocalizedNamesChanged,
                    languageTags = selectableLanguages,
                )
            }
        }
        // show only for adding new POIs because it gets too busy with also the name form
        // being displayed
        if (lastPickedFeatures.isNotEmpty() && element == null && selectedFeature == null) {
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
    }
}
