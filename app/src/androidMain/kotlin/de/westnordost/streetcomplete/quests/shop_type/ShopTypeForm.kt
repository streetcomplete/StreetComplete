package de.westnordost.streetcomplete.quests.shop_type

import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.POPULAR_PLACE_FEATURE_IDS
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.util.ktx.geometryType

class ShopTypeForm : AbstractOsmQuestForm<ShopTypeAnswer>() {

    @Composable
    override fun Content() {
        var feature by remember { mutableStateOf<Feature?>(null) }
        var option by remember { mutableStateOf<ShopTypeFormOption?>(null) }

        val isComplete = when (option) {
            null -> false
            ShopTypeFormOption.FEATURE -> feature != null
            else -> true
        }

        QuestForm(
            answers = Confirm(
                isComplete = isComplete,
                onClick = {
                    when (option) {
                        ShopTypeFormOption.FEATURE -> {
                            // if the shop has **some** name (that is displayed to the user), we just want to
                            // update the shop, not replace it. The train of thought is:
                            // 1. when the user is asked about what kind of shop <named thing> is, but doesn't
                            //    see any shop by that name, he will just answer that it doesn't exist via
                            //    Uh.. -> Doesn't exist.
                            // 2. When on the other hand he *does* see a shop by that name, it is quite clear
                            //    that it is still the same shop, so we only update it, not replace it.
                            // 3. On the other hand, if the place has no name, the user will also not be able
                            //    to answer whether the place is now a different one than before, so we rather
                            //    replace it. (#6675)
                            val hasSomeName = getNameLabel(element.tags) != null
                            applyAnswer(ShopType(feature!!, hasSomeName))
                        }
                        ShopTypeFormOption.VACANT -> applyAnswer(ShopTypeAnswer.IsShopVacant)
                        ShopTypeFormOption.LEAVE_NOTE -> composeNote()
                        null -> { }
                    }
                }
            )
        ) {
            ShopTypeForm(
                feature = feature,
                option = option,
                onSelectedFeature = { feature = it },
                onSelectedOption = { option = it },
                featureDictionary = featureDictionary,
                geometryType = element.geometryType,
                countryCode = countryOrSubdivisionCode,
                filterFn = { it.toElement().isPlace() },
                codesOfDefaultFeatures = POPULAR_PLACE_FEATURE_IDS
            )
        }
    }
}
