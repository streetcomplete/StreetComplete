package de.westnordost.streetcomplete.ui.common.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.no_search_results
import de.westnordost.streetcomplete.resources.quest_shop_gone_replaced_answer_hint
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import de.westnordost.streetcomplete.ui.common.ClearIcon
import de.westnordost.streetcomplete.ui.common.SearchIcon
import de.westnordost.streetcomplete.ui.ktx.fadingVerticalScrollEdges
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import org.jetbrains.compose.resources.stringResource

/** A search field and a list of results for features below. */
@Composable
fun FeatureSearch(
    onSelectedFeature: (Feature) -> Unit,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
    geometryType: GeometryType? = null,
    countryCode: String? = null,
    filterFn: (Feature) -> Boolean = { true },
    codesOfDefaultFeatures: List<String> = emptyList(),
) {
    val state = rememberLazyListState()

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    var search by remember { mutableStateOf("") }
    val languages = remember { getLanguagesForFeatureDictionary() }
    val defaultFeatures = remember(codesOfDefaultFeatures, featureDictionary, languages, countryCode) {
        codesOfDefaultFeatures.mapNotNull { id ->
            featureDictionary.getById(
                id = id,
                languages = languages,
                country = countryCode
            )
        }
    }
    val features = remember(search, featureDictionary, languages, countryCode, geometryType, filterFn, defaultFeatures) {
        if (search.isNotEmpty()) {
            featureDictionary.getByTerm(
                search = search,
                languages = languages,
                country = countryCode,
                geometry = geometryType,
            ).filter(filterFn).take(50).toList()
        } else defaultFeatures
    }

    Column(
        modifier = modifier
    ) {
        TextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            placeholder = {
                Text(stringResource(Res.string.quest_shop_gone_replaced_answer_hint))
            },
            leadingIcon = { SearchIcon() },
            trailingIcon = {
                if (search.isNotEmpty()) {
                    IconButton(onClick = { search = "" }) { ClearIcon() }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                showKeyboardOnFocus = true,
                imeAction = ImeAction.None,
                hintLocales = LocaleList.current,
            ),
        )
        Divider()
        if (features.isEmpty()) {
            CenteredLargeTitleHint(stringResource(Res.string.no_search_results))
        } else {
            FeaturesColumn(
                features = features,
                onClickFeature = onSelectedFeature,
                featureDictionary = featureDictionary,
                modifier = Modifier.fillMaxWidth(),
                countryCode = countryCode,
                searchText = search
            )
        }
    }
}

/** Show a list of features that can be selected */
@Composable
private fun FeaturesColumn(
    features: List<Feature>,
    onClickFeature: (Feature) -> Unit,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
    countryCode: String? = null,
    searchText: String? = null,
) {
    val state = rememberLazyListState()
    LazyColumn(
        modifier = modifier
            .fadingVerticalScrollEdges(state.scrollIndicatorState, 32.dp),
    ) {
        items(features) { feature ->
            Box(Modifier
                .fillMaxWidth()
                .clickable { onClickFeature(feature) }
            ) {
                FeatureItem(
                    feature = feature,
                    featureDictionary = featureDictionary,
                    countryCode = countryCode,
                    searchText = searchText,
                    iconSize = 22.5.dp
                )
            }
        }
    }
}
