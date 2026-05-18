package de.westnordost.streetcomplete.overlays.places

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.BaseFeature
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.POPULAR_PLACE_FEATURE_IDS
import de.westnordost.streetcomplete.osm.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.hasFixedName
import de.westnordost.streetcomplete.osm.isDisusedPlace
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.osm.localized_name.applyTo
import de.westnordost.streetcomplete.osm.localized_name.parseLocalizedNames
import de.westnordost.streetcomplete.osm.shouldReplacePlace
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.ui.common.feature.FeatureIcon
import de.westnordost.streetcomplete.ui.common.feature.FeatureSelect
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.common.localized_name.LocalizedNamesForm
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.LocalizedNameViewModel
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import org.koin.compose.viewmodel.koinViewModel

class PlacesOverlayForm : AbstractOverlayForm() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val namesViewModel = koinViewModel<LocalizedNameViewModel>()

        val lastPickedFeatures = remember {
            val languages = getLanguagesForFeatureDictionary()
            prefs.getLastPicked<String>(this::class.simpleName!!)
                .takeFavorites(n = 5, first = 1)
                .mapNotNull { featureId ->
                    featureDictionary.getById(
                        id = featureId,
                        languages = languages,
                        country = countryInfo.countryOrSubdivisionCode,
                    )
                }
        }

        val selectableLanguages = remember {
            namesViewModel.getLanguagesWithPreferredFirst(
                countryInfo.officialLanguages + countryInfo.additionalStreetsignLanguages
            )
        }

        val vacantShopFeature = remember {
            featureDictionary.getById("shop/vacant", getLanguagesForFeatureDictionary())!!
        }

        val unknownThingString = stringResource(Res.string.unknown_shop_title)
        val originalFeature = remember {
            element?.let { element ->
                getFeatureDictionaryFeature(element)
                ?: (if (element.isDisusedPlace()) vacantShopFeature else null)
                ?: BaseFeature(
                    id = "shop/unknown",
                    names = listOf(unknownThingString),
                    icon = "maki-shop",
                    tags = element.tags,
                    geometry = GeometryType.entries.toList()
                )
            }
        }
        val originalNoName = remember {
            element?.tags?.get("name:signed") == "no" || element?.tags?.get("noname") == "yes"
        }
        val originalNames = remember {
            parseLocalizedNames(element?.tags.orEmpty()).orEmpty()
        }

        var localizedNames by rememberSerializable {
            mutableStateOf(originalNames .takeIf { it.isNotEmpty() } ?: defaultNames())
        }
        var isNoName by rememberSaveable { mutableStateOf(originalNoName) }
        var selectedFeature by remember { mutableStateOf(originalFeature) }

        var askReplacePlace by remember { mutableStateOf(false) }

        fun onSelectedFeature(feature: Feature) {
            selectedFeature = feature
            isNoName = false
            // clear previous names (if necessary, and if any)
            if (feature.hasFixedName == true) {
                localizedNames = listOf()
            } else {
                localizedNames = defaultNames()
            }
        }

        fun onClickOk(replacePlace: Boolean) {
            namesViewModel.savePreferredLanguage(localizedNames)

            val newFeature = selectedFeature!!
            val inputNames = localizedNames.filter { it.name.isNotEmpty() }

            if (!newFeature.isSuggestion) {
                prefs.addLastPicked(this::class.simpleName!!, newFeature.id)
            }

            val tagChanges = StringMapChangesBuilder(element?.tags ?: emptyMap())

            if (replacePlace) {
                val isVacant = newFeature.id == "shop/vacant"
                if (isVacant) {
                    val vacantFeature = originalFeature?.toPrefixedFeature("disused") ?: newFeature
                    vacantFeature.applyReplacePlaceTo(tagChanges)
                } else {
                    newFeature.applyReplacePlaceTo(tagChanges)
                }
            } else {
                newFeature.applyTo(tagChanges, originalFeature)
            }

            if (!newFeature.hasFixedName) {
                // in this case name input was not even shown so newNames will be empty
                // newNames should not be applied as it will erase names provided by NSI
                inputNames.applyTo(tagChanges)
            }
            if (inputNames.isEmpty() && isNoName) {
                tagChanges["name:signed"] = "no"
            }

            val edit = if (element != null) {
                UpdateElementTagsAction(element!!, tagChanges.create())
            } else {
                CreateNodeAction(geometry.center, tagChanges)
            }

            applyEdit(edit)
        }

        OverlayForm(
            isComplete =
                // name is not necessary
                selectedFeature != null,
            hasChanges =
                originalFeature != selectedFeature
                || originalNames != localizedNames.filter { it.name.isNotEmpty() }
                || originalNoName != isNoName,
            onClickOk = {
                val feature = selectedFeature!!
                // new names could either be input by the user, or added by applying a (brand) preset
                val newNames = localizedNames
                    .filter { it.name.isNotEmpty() }
                    .takeIf { it.isNotEmpty() }
                    ?: parseLocalizedNames(feature.addTags).orEmpty()

                val shouldReplacePlace = shouldReplacePlace(
                    element = element,
                    newFeature = feature,
                    newNames = newNames,
                    previousFeature = originalFeature,
                    previousNames = originalNames
                )

                if (shouldReplacePlace != null) {
                    onClickOk(shouldReplacePlace)
                } else {
                    askReplacePlace = true
                }
            },
            label =
                // title hint label with name is a duplication, it is displayed in the UI already
                element?.let { nameAndLocationLabel(it, featureDictionary = null) },
            otherAnswers = listOfNotNull(
                if (originalFeature != vacantShopFeature) {
                    Answer(stringResource(Res.string.quest_shop_gone_vacant_answer))  {
                        val languages = getLanguagesForFeatureDictionary()
                        onSelectedFeature(featureDictionary.getById("shop/vacant", languages)!!)
                    }
                } else null,
                if (selectedFeature?.hasFixedName != true && !isNoName) {
                    Answer(stringResource(Res.string.quest_placeName_no_name_answer)) {
                        isNoName = true
                        localizedNames = listOf()
                    }
                } else null,

            )
        ) {
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 96.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                val feature = selectedFeature

                FeatureSelect(
                    feature = feature,
                    onSelectedFeature = ::onSelectedFeature,
                    featureDictionary = featureDictionary,
                    geometryType = element?.geometryType ?: GeometryType.POINT,
                    countryCode = countryInfo.countryOrSubdivisionCode,
                    filterFn = { it.toElement().isPlace() || it.id == "shop/vacant" },
                    codesOfDefaultFeatures = POPULAR_PLACE_FEATURE_IDS,
                )
                if (feature != null && !feature.hasFixedName) {
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
                            onChanged = {
                                localizedNames = it
                                if (it.isNotEmpty()) isNoName = false
                            },
                            languageTags = selectableLanguages,
                        )
                    }
                }
                // show only for adding new POIs becaues it gets too busy with also the name form
                // being displayed
                if (lastPickedFeatures.isNotEmpty() && element == null && selectedFeature == null) {
                    LastPickedChipsRow(
                        items = lastPickedFeatures,
                        onClick = { selectedFeature = it },
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

        if (askReplacePlace) {
            AskReplacePlaceDialog(
                onDismissRequest = { askReplacePlace = false },
                onAnswer = { replacePlace -> onClickOk(replacePlace) }
            )
        }
    }

    private fun getFeatureDictionaryFeature(element: Element): Feature? {
        val languages = getLanguagesForFeatureDictionary()
        val geometryType = if (element.type == ElementType.NODE) null else element.geometryType

        return featureDictionary.getByTags(
            tags = element.tags,
            languages = languages,
            country = countryInfo.countryOrSubdivisionCode,
            geometry = geometryType,
        ).firstOrNull { it.toElement().isPlace() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMarkerIcon(R.drawable.quest_shop)
    }

    private fun defaultNames(): List<LocalizedName> =
        listOf(LocalizedName(countryInfo.language.orEmpty(), ""))
}

@Composable
private fun AskReplacePlaceDialog(
    onDismissRequest: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
            TextButton(onClick = { onDismissRequest(); onAnswer(false) }) {
                Text(stringResource(Res.string.confirmation_replace_shop_no))
            }
            TextButton(onClick = { onDismissRequest(); onAnswer(true) }) {
                Text(stringResource(Res.string.confirmation_replace_shop_yes))
            }
        },
        title = { Text(stringResource(Res.string.confirmation_replace_shop_title)) },
        text = { Text(stringResource(Res.string.confirmation_replace_shop_message)) },
        modifier = modifier
    )
}
