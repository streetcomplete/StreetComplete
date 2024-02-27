package de.westnordost.streetcomplete.overlays.shops

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.Prefs.PREFERRED_LANGUAGE_FOR_NAMES
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.FragmentOverlayShopsBinding
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.osm.POPULAR_PLACE_FEATURE_IDS
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.isDisusedPlace
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.parseLocalizedNames
import de.westnordost.streetcomplete.osm.replacePlace
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.quests.LocalizedNameAdapter
import de.westnordost.streetcomplete.util.DummyFeature
import de.westnordost.streetcomplete.util.getLocalesForFeatureDictionary
import de.westnordost.streetcomplete.util.getLocationLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.prefs.Preferences
import de.westnordost.streetcomplete.view.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import kotlin.coroutines.resume

class ShopsOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_shops
    private val binding by contentViewBinding(FragmentOverlayShopsBinding::bind)

    private val prefs: Preferences by inject()

    private var originalFeature: Feature? = null
    private var originalNoName: Boolean = false
    private var originalNames: List<LocalizedName> = emptyList()

    private lateinit var featureCtrl: FeatureViewController
    private var isNoName: Boolean = false
    private var namesAdapter: LocalizedNameAdapter? = null

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_shop_gone_vacant_answer) { setVacant() },
        createNoNameAnswer()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val element = element
        originalFeature = element?.let {
            val locales = getLocalesForFeatureDictionary(resources.configuration)
            val geometryType = if (element.type == ElementType.NODE) null else element.geometryType

            if (element.isDisusedPlace()) {
                featureDictionary.byId("shop/vacant").forLocale(*locales).get()
            } else {
                featureDictionary
                    .byTags(element.tags)
                    .forLocale(*locales)
                    .forGeometry(geometryType)
                    .inCountry(countryOrSubdivisionCode)
                    .find()
                    .firstOrNull()
                // if not found anything in the iD presets, it's a shop type unknown to iD presets
                ?: DummyFeature(
                    "shop/unknown",
                    requireContext().getString(R.string.unknown_shop_title),
                    "maki-shop",
                    element.tags
                )
            }
        }
        originalNoName = element?.tags?.get("name:signed") == "no" || element?.tags?.get("noname") == "yes"
        isNoName = savedInstanceState?.getBoolean(NO_NAME) ?: originalNoName
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // title hint label with name is a duplication, it is displayed in the UI already
        setTitleHintLabel(element?.tags?.let { getLocationLabel(it, resources) })
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
                ::filterOnlyShops,
                ::onSelectedFeature,
                POPULAR_PLACE_FEATURE_IDS,
            ).show()
        }

        originalNames = parseLocalizedNames(element?.tags.orEmpty()).orEmpty()

        val persistedNames = savedInstanceState?.getString(LOCALIZED_NAMES_DATA)?.let { Json.decodeFromString<List<LocalizedName>>(it) }

        val selectableLanguages = (countryInfo.officialLanguages + countryInfo.additionalStreetsignLanguages).distinct().toMutableList()
        val preferredLanguage = prefs.getStringOrNull(PREFERRED_LANGUAGE_FOR_NAMES)
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

    private fun filterOnlyShops(feature: Feature): Boolean {
        val fakeElement = Node(-1L, LatLon(0.0, 0.0), feature.tags, 0)
        return fakeElement.isPlace() || feature.id == "shop/vacant"
    }

    private fun onSelectedFeature(feature: Feature) {
        featureCtrl.feature = feature
        // clear (previous) names if selected feature contains already a name (i.e. is a brand feature)
        // or is vacant
        if (feature.addTags?.get("name") != null || feature.id == "shop/vacant") {
            namesAdapter?.names = emptyList()
        }

        updateNameContainerVisibility()
        checkIsFormComplete()
    }

    private fun setVacant() {
        onSelectedFeature(featureDictionary.byId("shop/vacant").get())
    }

    private fun createNoNameAnswer(): AnswerItem? =
        if (featureCtrl.feature == null || isNoName) {
            null
        } else {
            AnswerItem(R.string.quest_placeName_no_name_answer) { setNoName() }
        }

    private fun setNoName() {
        isNoName = true
        namesAdapter?.names = listOf()
        updateNoNameHint()
    }

    private fun updateNameContainerVisibility() {
        val selectedFeature = featureCtrl.feature
        /* the name input is only visible if the place is not vacant, if a feature has been selected
           and if that feature doesn't already set a name (i.e. is a brand)
         */
        val isNameInputInvisible = selectedFeature == null ||
            selectedFeature.addTags?.get("name") != null ||
            selectedFeature.id == "shop/vacant"

        binding.nameContainer.root.isGone = isNameInputInvisible
        binding.nameLabel.isGone = isNameInputInvisible
    }

    private fun updateNoNameHint() {
        val showHint = isNoName && namesAdapter?.names?.isEmpty() == true
        namesAdapter?.emptyNamesHint = if (showHint) getString(R.string.quest_placeName_no_name_answer) else null
    }

    override fun hasChanges(): Boolean =
        originalFeature != featureCtrl.feature || originalNames != namesAdapter?.names
        || originalNoName != isNoName

    override fun isFormComplete(): Boolean =
        featureCtrl.feature != null // name is not necessary

    override fun onClickOk() {
        val firstLanguage = namesAdapter?.names?.firstOrNull()?.languageTag?.takeIf { it.isNotBlank() }
        if (firstLanguage != null) prefs.putString(PREFERRED_LANGUAGE_FOR_NAMES, firstLanguage)

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

private suspend fun createEditAction(
    element: Element?,
    geometry: ElementGeometry,
    newNames: List<LocalizedName>,
    previousNames: List<LocalizedName>,
    newFeature: Feature,
    previousFeature: Feature?,
    isNoName: Boolean,
    confirmReplaceShop: suspend () -> Boolean
): ElementEditAction {
    val tagChanges = StringMapChangesBuilder(element?.tags ?: emptyMap())

    val hasAddedNames = previousNames.isEmpty() && newNames.isNotEmpty()
    val hasChangedNames = previousNames != newNames
    val hasChangedFeature = newFeature != previousFeature
    val isFeatureWithName = newFeature.addTags?.get("name") != null
    val wasFeatureWithName = previousFeature?.addTags?.get("name") != null
    val wasVacant = element != null && element.isDisusedPlace()
    val isVacant = newFeature.id == "shop/vacant"

    val doReplaceShop =
        // do not replace shop if:
        if (
            // only a name was added (name was missing before; user wouldn't be able to answer
            // if the place changed or not anyway, so rather keep previous information)
            hasAddedNames && !hasChangedFeature
            // previously: only the feature was changed, the non-empty name did not change
            // - see #5195
            // place has been added, nothing to replace
            || element == null
        ) {
            false
        }
        // always replace if:
        else if (
            // the feature is a brand feature or was a brand feature (i.e. overwrites the name)
            isFeatureWithName || wasFeatureWithName
            // was vacant before but not anymore (-> cleans up any previous tags that may be
            // associated with the old place
            || wasVacant && hasChangedFeature
            // it's vacant now
            || isVacant
        ) {
            true
        }
        // ask whether it is still the same shop if:
        // + the name was changed
        // + the feature was changed and the name was empty before
        else {
            confirmReplaceShop()
        }

    if (doReplaceShop) {
        tagChanges.replacePlace(newFeature.addTags)
    } else {
        for ((key, value) in previousFeature?.removeTags.orEmpty()) {
            tagChanges.remove(key)
        }
        for ((key, value) in newFeature.addTags) {
            tagChanges[key] = value
        }
    }

    if (!isFeatureWithName) {
        // in this case name input was not even shown so newNames will be empty
        // newNames should not be applied as it will erase names provided by NSI
        newNames.applyTo(tagChanges)
    }
    if (newNames.isEmpty() && isNoName) {
        tagChanges["name:signed"] = "no"
    }

    return if (element != null) {
        UpdateElementTagsAction(element, tagChanges.create())
    } else {
        CreateNodeAction(geometry.center, tagChanges)
    }
}
