package de.westnordost.streetcomplete.quests.shop_type

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeFormOption.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_shop_gone_replaced_answer
import de.westnordost.streetcomplete.resources.quest_shop_gone_vacant_answer
import de.westnordost.streetcomplete.resources.quest_streetName_answer_noProperStreet_leaveNote
import de.westnordost.streetcomplete.ui.common.RadioGroup
import de.westnordost.streetcomplete.ui.common.feature.FeatureSelect
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Form to select what's up with a shop/place: Is it a certain type of shop, is it vacant, or
 *  something else? */
@Composable
fun ShopTypeForm(
    feature: Feature?,
    option: ShopTypeFormOption?,
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
        selectedOption = option,
        itemContent = {
            Row {
                Text(stringResource(it.text))
                if (it == FEATURE) {
                    FeatureSelect(
                        feature = feature,
                        onSelectedFeature = onSelectedFeature,
                        featureDictionary = featureDictionary,
                        geometryType = geometryType,
                        countryCode = countryCode,
                        filterFn = filterFn,
                        codesOfDefaultFeatures = codesOfDefaultFeatures,
                    )
                }
            }
        }
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
