package de.westnordost.streetcomplete.overlays.places

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.addLastPicked
import de.westnordost.streetcomplete.data.preferences.getLastPicked
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.hasFixedName
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.osm.localized_name.applyTo
import de.westnordost.streetcomplete.osm.localized_name.parseLocalizedNames
import de.westnordost.streetcomplete.osm.places.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.places.getPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.places.isDisusedPlace
import de.westnordost.streetcomplete.osm.places.shouldReplacePlace
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable fun PlacesOverlayForm(
    on: (OverlayAction) -> Unit,
    element: Element?,
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
    preferences: Preferences = koinInject(),
    featureDictionary: FeatureDictionary = koinInject(),
) {
    val favKey = "PlacesOverlayForm"
    val lastPickedFeatures = remember {
        preferences.getLastPicked<String>(favKey)
            .takeFavorites(n = 5, first = 1)
            .mapNotNull { featureId ->
                featureDictionary.getById(
                    id = featureId,
                    languages = getLanguagesForFeatureDictionary(),
                    country = countryInfo.countryOrSubdivisionCode,
                )
            }
    }

    val selectableLanguages = remember {
        preferences.getLanguagesWithPreferredFirst(
            countryInfo.officialLanguages + countryInfo.additionalStreetsignLanguages
        )
    }

    val unknownPlaceString = stringResource(Res.string.unknown_shop_title)
    val disusedString = stringResource(Res.string.disused).uppercase()
    val originalFeature = remember(element) {
        element?.let {
            featureDictionary.getPlaceOrDisusedPlace(
                disusedString = disusedString,
                unknownPlaceString = unknownPlaceString,
                element = element,
                country = countryInfo.countryOrSubdivisionCode
            )
        }
    }
    val originalNoName = remember(element) {
        element?.tags?.get("name:signed") == "no" || element?.tags?.get("noname") == "yes"
    }
    val originalNames = remember(element) {
        parseLocalizedNames(element?.tags.orEmpty()).orEmpty()
    }

    var localizedNames by rememberSerializable(originalNames) { mutableStateOf(
        originalNames.takeIf { it.isNotEmpty() }
            ?: originalFeature?.addTags?.let { parseLocalizedNames(it) }
            ?: listOf(LocalizedName(countryInfo.language.orEmpty(), ""))
    ) }
    var isNoName by rememberSaveable(originalNoName) { mutableStateOf(originalNoName) }
    var selectedFeature by remember(originalFeature) { mutableStateOf(originalFeature) }

    var askReplacePlace by remember { mutableStateOf(false) }

    fun onSelectedFeature(feature: Feature) {
        selectedFeature = feature
        isNoName = false
        // clear previous names (if necessary, and if any)
        if (feature.hasFixedName == true) {
            localizedNames = listOf()
        } else {
            localizedNames =
                parseLocalizedNames(feature.addTags)
                ?: listOf(LocalizedName(countryInfo.language.orEmpty(), ""))
        }
    }

    fun onClickOk(replacePlace: Boolean) {
        preferences.preferredLanguageForNames =
            localizedNames.firstOrNull()?.languageTag?.takeIf { it.isNotEmpty() }

        val newFeature = selectedFeature!!
        val inputNames = localizedNames.filter { it.name.isNotEmpty() }

        if (!newFeature.isSuggestion) {
            preferences.addLastPicked(favKey, newFeature.id)
        }

        val tagChanges = StringMapChangesBuilder(element?.tags ?: emptyMap())

        if (replacePlace) {
            newFeature.applyReplacePlaceTo(tagChanges)
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
            UpdateElementTagsAction(element, tagChanges.create())
        } else {
            CreateNodeAction(geometry.center, tagChanges)
        }

        on(Edit(edit))
    }

    OverlayForm(
        on = on,
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
        otherAnswers = { listOfNotNull(
            if (originalFeature?.isDisusedPlace() != false) {
                AnswerItem(stringResource(Res.string.quest_shop_gone_vacant_answer))  {
                    val languages = getLanguagesForFeatureDictionary()
                    onSelectedFeature(featureDictionary.getById("shop/vacant", languages)!!)
                }
            } else null,
            if (selectedFeature?.hasFixedName != true && !isNoName) {
                AnswerItem(stringResource(Res.string.quest_placeName_no_name_answer)) {
                    isNoName = true
                    localizedNames = listOf()
                }
            } else null,
        ) },
    ) {
        PlaceForm(
            selectedFeature = selectedFeature,
            onSelectedFeature = ::onSelectedFeature,
            lastPickedFeatures = lastPickedFeatures,
            localizedNames = localizedNames,
            isNoName = isNoName,
            selectableLanguages = selectableLanguages,
            onLocalizedNamesChanged = {
                localizedNames = it
                if (it.isNotEmpty()) isNoName = false
            },
            element = element,
            countryCode = countryInfo.countryOrSubdivisionCode,
            featureDictionary = featureDictionary,
        )
    }

    if (askReplacePlace) {
        AskReplacePlaceDialog(
            onDismissRequest = { askReplacePlace = false },
            onAnswer = { replacePlace -> onClickOk(replacePlace) }
        )
    }
}
