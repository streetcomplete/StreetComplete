package de.westnordost.streetcomplete.quests.shop_type

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.places.POPULAR_PLACE_FEATURE_IDS
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.ktx.geometryType
import org.koin.compose.koinInject

@Composable
fun ShopTypeQuestForm(
    on: (QuestAction<ShopTypeAnswer>) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
    otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
    featureDictionary: FeatureDictionary = koinInject(),
) {
    var feature by remember { mutableStateOf<Feature?>(null) }
    var option by remember { mutableStateOf<ShopTypeFormOption?>(null) }

    val isComplete = when (option) {
        null -> false
        ShopTypeFormOption.FEATURE -> feature != null
        else -> true
    }

    fun onClickOk() {
        when (option) {
            ShopTypeFormOption.FEATURE -> on(Answer(ShopType(feature!!)))
            ShopTypeFormOption.VACANT -> on(Answer(ShopTypeAnswer.IsShopVacant))
            ShopTypeFormOption.LEAVE_NOTE -> on(Action.LeaveNote)
            null -> { }
        }
    }

    QuestForm(
        isComplete = isComplete,
        onClickOk = ::onClickOk,
        on = on,
        modifier = modifier,
        otherAnswers = otherAnswers
    ) {
        ShopTypeForm(
            feature = feature,
            option = option,
            onSelectedFeature = { feature = it },
            onSelectedOption = { option = it },
            featureDictionary = featureDictionary,
            geometryType = element.geometryType,
            countryCode = countryInfo.countryOrSubdivisionCode,
            filterFn = { it.toElement().isPlace() },
            codesOfDefaultFeatures = POPULAR_PLACE_FEATURE_IDS
        )
    }
}
