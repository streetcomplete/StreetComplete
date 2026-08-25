package de.westnordost.streetcomplete.quests.shop_type

import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.osm.places.POPULAR_PLACE_FEATURE_IDS
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.stringResource

/** Dialog in which to select what happened with a shop that doesn't exist anymore */
@Composable
fun ShopGoneDialog(
    onDismissRequest: () -> Unit,
    onSelectAnswer: (ShopTypeAnswer) -> Unit,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
    geometryType: GeometryType? = null,
    countryCode: String? = null,
) {
    var feature: Feature? by remember { mutableStateOf(null) }
    var option: ShopTypeFormOption? by remember { mutableStateOf(null) }

    fun onClickOk() {
        when (option) {
            ShopTypeFormOption.FEATURE -> onSelectAnswer(ShopType(feature!!))
            ShopTypeFormOption.VACANT -> onSelectAnswer(ShopTypeAnswer.IsShopVacant)
            ShopTypeFormOption.LEAVE_NOTE -> onSelectAnswer(ShopTypeAnswer.LeaveNote)
            null -> { }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val feature = feature
            TextButton(
                onClick = {
                    onClickOk()
                    onDismissRequest()
                },
                enabled = when (option) {
                    null -> false
                    ShopTypeFormOption.FEATURE -> feature != null
                    else -> true
                }
            ) {
                Text(stringResource(Res.string.ok))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
        },
        title = {
            Text(stringResource(Res.string.quest_shop_gone_title))
        },
        text = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                ShopTypeForm(
                    feature = feature,
                    selectedOption = option,
                    onSelectedFeature = { feature = it },
                    onSelectedOption = { option = it },
                    featureDictionary = featureDictionary,
                    geometryType = geometryType,
                    countryCode = countryCode,
                    filterFn = { it.toElement().isPlace() },
                    codesOfDefaultFeatures = POPULAR_PLACE_FEATURE_IDS
                )
            }
        }
    )
}
