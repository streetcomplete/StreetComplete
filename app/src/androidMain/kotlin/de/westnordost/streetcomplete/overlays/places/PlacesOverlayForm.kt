package de.westnordost.streetcomplete.overlays.places

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.isGone
import de.westnordost.osmfeatures.BaseFeature
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.POPULAR_PLACE_FEATURE_IDS
import de.westnordost.streetcomplete.osm.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.isDisusedPlace
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.osm.localized_name.applyTo
import de.westnordost.streetcomplete.osm.localized_name.parseLocalizedNames
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.name_label
import de.westnordost.streetcomplete.resources.quest_placeName_no_name_answer
import de.westnordost.streetcomplete.ui.common.feature.FeatureIcon
import de.westnordost.streetcomplete.ui.common.feature.FeatureSelect
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.common.localized_name.LocalizedNamesForm
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.getLocationSpanned
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.takeFavorites
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import kotlin.coroutines.resume

class PlacesOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()

    private var originalFeature: Feature? = null
    private var originalNoName: Boolean = false
    private var originalNames: List<LocalizedName> = emptyList()
    private var selectedFeature: MutableState<Feature?> = mutableStateOf(null)
    private var localizedNames: MutableState<List<LocalizedName>> = mutableStateOf(emptyList())
    private var isNoName: MutableState<Boolean> = mutableStateOf(false)

    private val lastPickedFeatures: List<Feature> by lazy {
        val languages = getLanguagesForFeatureDictionary()
        prefs.getLastPicked<String>(this::class.simpleName!!)
            .takeFavorites(n = 5, first = 1)
            .mapNotNull { featureId ->
                featureDictionary.getById(
                    id = featureId,
                    languages = languages,
                    country = countryOrSubdivisionCode,
                )
            }
    }


    private lateinit var vacantShopFeature: Feature

    override val otherAnswers get() = listOfNotNull(
        createVacantAnswer(),
        createNoNameAnswer()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languages = getLanguagesForFeatureDictionary()
        vacantShopFeature = featureDictionary.getById("shop/vacant", languages)!!
        originalNames = parseLocalizedNames(element?.tags.orEmpty()).orEmpty()
        originalFeature = getOriginalFeature()
        selectedFeature.value = originalFeature
        originalNoName = element?.tags?.get("name:signed") == "no" || element?.tags?.get("noname") == "yes"
    }

    private fun getOriginalFeature(): Feature? {
        val element = element ?: return null

        return getFeatureDictionaryFeature(element)
            ?: (if (element.isDisusedPlace()) vacantShopFeature else null)
            ?: BaseFeature(
                id = "shop/unknown",
                names = listOf(requireContext().getString(R.string.unknown_shop_title)),
                icon = "maki-shop",
                tags = element.tags,
                geometry = GeometryType.entries.toList()
            )
    }

    private fun getFeatureDictionaryFeature(element: Element): Feature? {
        val languages = getLanguagesForFeatureDictionary()
        val geometryType = if (element.type == ElementType.NODE) null else element.geometryType

        return featureDictionary.getByTags(
            tags = element.tags,
            languages = languages,
            country = countryOrSubdivisionCode,
            geometry = geometryType,
        ).firstOrNull { it.toElement().isPlace() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // title hint label with name is a duplication, it is displayed in the UI already
        setTitleHintLabel(element?.tags?.let { getLocationSpanned(it, resources) })
        setMarkerIcon(R.drawable.quest_shop)

        val selectableLanguages = (
            countryInfo.officialLanguages + countryInfo.additionalStreetsignLanguages
        ).distinct().toMutableList()
        val preferredLanguage = prefs.preferredLanguageForNames
        if (preferredLanguage != null) {
            if (selectableLanguages.remove(preferredLanguage)) {
                selectableLanguages.add(0, preferredLanguage)
            }
        }

        binding.composeViewBase.content { Surface {
            localizedNames = rememberSerializable {
                mutableStateOf(originalNames.takeIf { it.isNotEmpty() } ?: defaultNames())
            }
            isNoName = rememberSaveable { mutableStateOf(originalNoName) }

            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 96.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                val feature = selectedFeature.value

                FeatureSelect(
                    feature = feature,
                    onSelectedFeature = ::onSelectedFeature,
                    featureDictionary = featureDictionary,
                    geometryType = element?.geometryType ?: GeometryType.POINT,
                    countryCode = countryOrSubdivisionCode,
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
                        if (isNoName.value && localizedNames.value.isEmpty()) {
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
                            localizedNames = localizedNames.value,
                            onChanged = {
                                localizedNames.value = it
                                if (it.isNotEmpty()) isNoName.value = false
                                checkIsFormComplete()
                            },
                            languageTags = selectableLanguages,
                        )
                    }
                }
                // show only for adding new POIs becaues it gets too busy with also the name form
                // being displayed
                if (lastPickedFeatures.isNotEmpty() && element == null && selectedFeature.value == null) {
                    LastPickedChipsRow(
                        items = lastPickedFeatures,
                        onClick = {
                            selectedFeature.value = it
                            checkIsFormComplete()
                        },
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
        } }
        checkIsFormComplete()
    }

    private fun onSelectedFeature(feature: Feature) {
        selectedFeature.value = feature
        isNoName.value = false
        // clear previous names (if necessary, and if any)
        if (feature.hasFixedName == true) {
            localizedNames.value = listOf()
        } else {
            localizedNames.value = defaultNames()
        }
        checkIsFormComplete()
    }

    private fun setVacant() {
        val languages = getLanguagesForFeatureDictionary()
        onSelectedFeature(featureDictionary.getById("shop/vacant", languages)!!)
    }

    private fun createNoNameAnswer(): AnswerItem? {
        val feature = selectedFeature.value
        return if (feature == null || isNoName.value || feature.hasFixedName) {
            null
        } else {
            AnswerItem(R.string.quest_placeName_no_name_answer) { setNoName() }
        }
    }

    private fun createVacantAnswer(): AnswerItem? = if (originalFeature == vacantShopFeature) {
            null
        } else {
            AnswerItem(R.string.quest_shop_gone_vacant_answer)  { setVacant() }
        }

    private fun setNoName() {
        isNoName.value = true
        localizedNames.value = listOf()
        checkIsFormComplete()
    }

    private fun defaultNames(): List<LocalizedName> =
        listOf(LocalizedName(countryInfo.language.orEmpty(), ""))

    override fun hasChanges(): Boolean =
        originalFeature != selectedFeature.value
        || originalNames != localizedNames.value.filter { it.name.isNotEmpty() }
        || originalNoName != isNoName.value

    override fun isFormComplete(): Boolean =
        selectedFeature.value != null
        // name is not necessary

    override fun onClickOk() {
        val inputNames = localizedNames.value.filter { it.name.isNotEmpty() }
        val firstLanguage = inputNames.firstOrNull()?.languageTag
        if (!firstLanguage.isNullOrEmpty()) prefs.preferredLanguageForNames = firstLanguage

        val feature = selectedFeature.value!!
        if (!feature.isSuggestion) {
            prefs.addLastPicked(this::class.simpleName!!, feature.id)
        }

        viewLifecycleScope.launch {
            applyEdit(createEditAction(
                element, geometry,
                inputNames, originalNames,
                selectedFeature.value!!, originalFeature,
                isNoName.value,
                ::confirmReplaceShop
            ))
        }
    }

    private suspend fun confirmReplaceShop(): Boolean = suspendCancellableCoroutine { cont ->
        val dlg = AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirmation_replace_shop_title)
            .setMessage(R.string.confirmation_replace_shop_message)
            .setPositiveButton(R.string.confirmation_replace_shop_yes) { _, _ -> cont.resume(true) }
            .setNegativeButton(R.string.confirmation_replace_shop_no) { _, _ -> cont.resume(false) }
            .create()
        cont.invokeOnCancellation { dlg.cancel() }
        dlg.show()
    }
}

/** return the id of the feature, without any brand stuff */
private val Feature.featureId get() = if (isSuggestion) id.substringBeforeLast("/") else id

/** Whether this feature is a subtype of another feature */
private fun Feature.isChildOf(other: Feature): Boolean =
    id.startsWith(other.id)

/** return whether the feature has a fixed name which cannot be changed */
private val Feature.hasFixedName get() =
    addTags.containsKey("name") && preserveTags.none { it.containsMatchIn("name") }
    || id == "shop/vacant"
    || id == "shop/unknown"

private suspend fun createEditAction(
    element: Element?,
    geometry: ElementGeometry,
    inputNames: List<LocalizedName>,
    previousNames: List<LocalizedName>,
    newFeature: Feature,
    previousFeature: Feature?,
    isNoName: Boolean,
    confirmReplaceShop: suspend () -> Boolean
): ElementEditAction {
    val tagChanges = StringMapChangesBuilder(element?.tags ?: emptyMap())

    // new names could either be input by the user, or added by applying a (brand) preset
    val newNames = inputNames.takeIf { it.isNotEmpty() }
        ?: parseLocalizedNames(newFeature.addTags).orEmpty()

    val hasAddedNames = newNames.isNotEmpty() && newNames.containsAll(previousNames)
    val hasChangedNames = previousNames != newNames
    val hasChangedId = newFeature.id != previousFeature?.id
    val hasChangedFeatureId = previousFeature?.featureId != newFeature.featureId
    val isFeatureSubtype =
        previousFeature != null
        && (newFeature.isChildOf(previousFeature) || previousFeature.isChildOf(newFeature))
    val wasVacant = element != null && element.isDisusedPlace()
    val isVacant = newFeature.id == "shop/vacant"
    val wasBrand =  previousFeature?.isSuggestion == true
    val isBrand =  newFeature.isSuggestion

    val shouldNotReplaceShop =
        // a brand preset was applied, but neither names nor feature type changed (see #5940)
        !hasChangedNames && !hasChangedFeatureId
        // name(s) were added but feature wasn't changed at all; user wouldn't be able to answer if
        // the place changed or not anyway, so rather keep previous information
        || hasAddedNames && !hasChangedId
        // place has been added, nothing to replace
        || element == null
    val shouldAlwaysReplaceShop =
        // the feature is or was a brand feature and the type has changed -> definitely different
        // place now; If the name and/or feature changed, the user might just have corrected the
        // spelling or corrected the type (e.g. kindergarten -> childcare), so it is better to ask.
        // Also, a place might have been tagged as fast food before and now it is pizza fast food,
        // this should not lead to auto-replacing (see #6406)
        (isBrand || wasBrand) && !isFeatureSubtype
        // was vacant before but not anymore (-> cleans up any previous tags that may be
        // associated with the old place)
        || wasVacant && hasChangedId
        // it's vacant now
        || isVacant

    val doReplaceShop =
        if (shouldNotReplaceShop) {
            false
        } else if (shouldAlwaysReplaceShop) {
            true
        } else {
            confirmReplaceShop()
        }

    if (doReplaceShop) {
        if (isVacant) {
            val vacantFeature = previousFeature?.toPrefixedFeature("disused") ?: newFeature
            vacantFeature.applyReplacePlaceTo(tagChanges)
        } else {
            newFeature.applyReplacePlaceTo(tagChanges)
        }
    } else {
        newFeature.applyTo(tagChanges, previousFeature)
    }

    if (!newFeature.hasFixedName) {
        // in this case name input was not even shown so newNames will be empty
        // newNames should not be applied as it will erase names provided by NSI
        inputNames.applyTo(tagChanges)
    }
    if (inputNames.isEmpty() && isNoName) {
        tagChanges["name:signed"] = "no"
    }

    return if (element != null) {
        UpdateElementTagsAction(element, tagChanges.create())
    } else {
        CreateNodeAction(geometry.center, tagChanges)
    }
}
