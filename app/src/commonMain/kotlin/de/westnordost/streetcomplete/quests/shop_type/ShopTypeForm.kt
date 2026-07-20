package de.westnordost.streetcomplete.quests.shop_type

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeFormOption.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.RadioGroup
import de.westnordost.streetcomplete.ui.common.feature.FeatureSelect
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Form to select what's up with a shop/place: Is it a certain type of shop, is it vacant, or
 *  something else? */
@Composable
fun ShopTypeForm(
    feature: Feature?,
    selectedOption: ShopTypeFormOption?,
    onSelectedFeature: (Feature) -> Unit,
    onSelectedOption: (ShopTypeFormOption) -> Unit,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
    geometryType: GeometryType? = null,
    countryCode: String? = null,
    filterFn: (Feature) -> Boolean = { true },
    codesOfDefaultFeatures: List<String> = emptyList(),
) {
    RadioGroup(
        options = ShopTypeFormOption.entries,
        onSelectionChange = onSelectedOption,
        selectedOption = selectedOption,
        itemContent = { option ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(option.text))
                if (option == FEATURE) {
                    FeatureSelect(
                        feature = feature,
                        onSelectedFeature = { feature ->
                            onSelectedOption(FEATURE)
                            onSelectedFeature(feature)
                        },
                        featureDictionary = featureDictionary,
                        geometryType = geometryType,
                        countryCode = countryCode,
                        filterFn = filterFn,
                        codesOfDefaultFeatures = codesOfDefaultFeatures,
                    )
                }
            }
        },
        modifier = modifier,
    )
}

enum class ShopTypeFormOption {
    FEATURE,
    VACANT,
    LEAVE_NOTE
}

private val ShopTypeFormOption.text: StringResource get() = when (this) {
    FEATURE -> Res.string.quest_shop_gone_replaced_answer
    VACANT -> Res.string.quest_shop_gone_vacant_answer
    LEAVE_NOTE -> Res.string.quest_streetName_answer_noProperStreet_leaveNote
}
