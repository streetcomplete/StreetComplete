package de.westnordost.streetcomplete.overlays.places

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
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
import de.westnordost.streetcomplete.databinding.FragmentOverlayPlacesBinding
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.osm.POPULAR_PLACE_FEATURE_IDS
import de.westnordost.streetcomplete.osm.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.isDisusedPlace
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.parseLocalizedNames
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.quests.LocalizedNameAdapter
import de.westnordost.streetcomplete.util.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.getLocationSpanned
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import kotlin.coroutines.resume

class PlacesOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_places
    private val binding by contentViewBinding(FragmentOverlayPlacesBinding::bind)

    private val prefs: Preferences by inject()

    private var originalFeature: Feature? = null
    private var originalNoName: Boolean = false
    private var originalNames: List<LocalizedName> = emptyList()

    private lateinit var featureCtrl: FeatureViewController
    private var isNoName: Boolean = false
    private var namesAdapter: LocalizedNameAdapter? = null

    private lateinit var vacantShopFeature: Feature

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_shop_gone_vacant_answer) { setVacant() },
        createNoNameAnswer()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languages = getLanguagesForFeatureDictionary(resources.configuration)
        vacantShopFeature = featureDictionary.getById("shop/vacant", languages)!!
        originalFeature = getOriginalFeature()
        originalNoName = element?.tags?.get("name:signed") == "no" || element?.tags?.get("noname") == "yes"
        isNoName = savedInstanceState?.getBoolean(NO_NAME) ?: originalNoName
    }

    private fun getOriginalFeature(): Feature? {
        val element = element ?: return null

        return getFeatureDictionaryFeature(element)
            ?: if (element.isDisusedPlace()) vacantShopFeature else null
            ?: BaseFeature(
                id = "shop/unknown",
                names = listOf(requireContext().getString(R.string.unknown_shop_title)),
                icon = "maki-shop",
                tags = element.tags,
                geometry = GeometryType.entries.toList()
            )
    }

    private fun getFeatureDictionaryFeature(element: Element): Feature? {
        val languages = getLanguagesForFeatureDictionary(resources.configuration)
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
        setMarkerIcon(R.drawable.ic_quest_shop)

        featureCtrl = FeatureViewController(featureDictionary, binding.featureTextView, binding.featureIconView)
        featureCtrl.countryOrSubdivisionCode = countryOrSubdivisionCode
        featureCtrl.feature = originalFeature

        binding.featureView.setOnClickListener {
            SearchFeaturesDialog(
                requireContext(),
                featureDictionary,
                element?.geometryType ?: GeometryType.POINT,
                countryOrSubdivisionCode,
                featureCtrl.feature?.name,
                { it.toElement().isPlace() || it.id == "shop/vacant" },
                ::onSelectedFeature,
                POPULAR_PLACE_FEATURE_IDS,
            ).show()
        }

        originalNames = parseLocalizedNames(element?.tags.orEmpty()).orEmpty()

        val persistedNames = savedInstanceState?.getString(LOCALIZED_NAMES_DATA)?.let {
            Json.decodeFromString<List<LocalizedName>>(it)
        }

        val selectableLanguages = (
            countryInfo.officialLanguages + countryInfo.additionalStreetsignLanguages
        ).distinct().toMutableList()

        val preferredLanguage = prefs.preferredLanguageForNames
        if (preferredLanguage != null) {
            if (selectableLanguages.remove(preferredLanguage)) {
                selectableLanguages.add(0, preferredLanguage)
            }
        }

        val adapter = LocalizedNameAdapter(
            persistedNames ?: originalNames.map { it.copy() },
            requireContext(),
            selectableLanguages,
            null,
            null,
            binding.nameContainer.addLanguageButton
        )
        adapter.addOnNameChangedListener { checkIsFormComplete() }
        adapter.registerAdapterDataObserver(AdapterDataChangedWatcher { checkIsFormComplete() })
        lifecycle.addObserver(adapter)
        namesAdapter = adapter
        binding.nameContainer.namesList.adapter = adapter
        binding.nameContainer.namesList.isNestedScrollingEnabled = false

        updateNameContainerVisibility()
        updateNoNameHint()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        namesAdapter?.names?.let { outState.putString(LOCALIZED_NAMES_DATA, Json.encodeToString(it)) }
        outState.putBoolean(NO_NAME, isNoName)
    }

    private fun onSelectedFeature(feature: Feature) {
        featureCtrl.feature = feature
        // clear previous names (if necessary, and if any)
        if (feature.hasFixedName) namesAdapter?.names = emptyList()
        updateNameContainerVisibility()
        checkIsFormComplete()
    }

    private fun setVacant() {
        val languages = getLanguagesForFeatureDictionary(resources.configuration)
        onSelectedFeature(featureDictionary.getById("shop/vacant", languages)!!)
    }

    private fun createNoNameAnswer(): AnswerItem? {
        val feature = featureCtrl.feature
        return if (feature == null || isNoName || feature.hasFixedName) {
            null
        } else {
            AnswerItem(R.string.quest_placeName_no_name_answer) { setNoName() }
        }
    }

    private fun setNoName() {
        isNoName = true
        namesAdapter?.names = listOf()
        updateNoNameHint()
    }

    private fun updateNameContainerVisibility() {
        val feature = featureCtrl.feature
        val isNameInputInvisible = feature == null || feature.hasFixedName

        binding.nameContainer.root.isGone = isNameInputInvisible
        binding.nameLabel.isGone = isNameInputInvisible
    }

    private fun updateNoNameHint() {
        val showHint = isNoName && namesAdapter?.names?.isEmpty() == true
        namesAdapter?.emptyNamesHint = if (showHint) getString(R.string.quest_placeName_no_name_answer) else null
    }

    override fun hasChanges(): Boolean =
        originalFeature != featureCtrl.feature
        || originalNames != namesAdapter?.names
        || originalNoName != isNoName

    override fun isFormComplete(): Boolean =
        featureCtrl.feature != null // name is not necessary

    override fun onClickOk() {
        val firstLanguage = namesAdapter?.names?.firstOrNull()?.languageTag?.takeIf { it.isNotBlank() }
        if (firstLanguage != null) prefs.preferredLanguageForNames = firstLanguage

        viewLifecycleScope.launch {
            applyEdit(createEditAction(
                element, geometry,
                namesAdapter?.names.orEmpty(), originalNames,
                featureCtrl.feature!!, originalFeature,
                isNoName,
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

    companion object {
        private const val LOCALIZED_NAMES_DATA = "localized_names_data"
        private const val NO_NAME = "NO_NAME"
    }
}

/** return the id of the feature, without any brand stuff */
private val Feature.featureId get() = if (isSuggestion) id.substringBeforeLast("/") else id

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
    val hasChangedFeature = newFeature.id != previousFeature?.id
    val hasChangedFeatureType = previousFeature?.featureId != newFeature.featureId
    val wasVacant = element != null && element.isDisusedPlace()
    val isVacant = newFeature.id == "shop/vacant"
    val wasBrand =  previousFeature?.isSuggestion == true
    val isBrand =  newFeature.isSuggestion

    val shouldNotReplaceShop =
        // a brand preset was applied, but neither names nor feature type changed (see #5940)
        !hasChangedNames && !hasChangedFeatureType
        // name(s) were added but feature wasn't changed at all; user wouldn't be able to answer if
        // the place changed or not anyway, so rather keep previous information
        || hasAddedNames && !hasChangedFeature
        // place has been added, nothing to replace
        || element == null
    val shouldAlwaysReplaceShop =
        // the feature is or was a brand feature and the type has changed -> definitely different
        // place now; If the name and/or feature changed, the user might just have corrected the
        // spelling or corrected the type (e.g. kindergarten -> childcare), so it is better to ask
        (isBrand || wasBrand) && hasChangedFeatureType
        // was vacant before but not anymore (-> cleans up any previous tags that may be
        // associated with the old place)
        || wasVacant && hasChangedFeature
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
