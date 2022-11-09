package de.westnordost.streetcomplete.overlays.shops

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.view.isGone
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.Prefs.PREFERRED_LANGUAGE_FOR_NAMES
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.FragmentOverlayShopsBinding
import de.westnordost.streetcomplete.osm.IS_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.createLocalizedNames
import de.westnordost.streetcomplete.osm.replaceShop
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.LocalizedNameAdapter
import de.westnordost.streetcomplete.util.getLocalesForFeatureDictionary
import de.westnordost.streetcomplete.util.getLocationLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import kotlin.coroutines.resume

class ShopsOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_shops
    private val binding by contentViewBinding(FragmentOverlayShopsBinding::bind)

    private val prefs: SharedPreferences by inject()

    private lateinit var featureCtrl: FeatureViewController

    private var feature: Feature? = null
    private var names: List<LocalizedName> = emptyList()

    private var adapter: LocalizedNameAdapter? = null

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_shop_gone_vacant_answer) { setVacant() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val element = element
        feature = element?.let {
            if (IS_DISUSED_SHOP_EXPRESSION.matches(element)) {
                createVacantShop(requireContext().resources)
            } else {
                featureDictionary
                    .byTags(element.tags)
                    .forLocale(*getLocalesForFeatureDictionary(resources.configuration))
                    .forGeometry(element.geometryType)
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // title hint label with name is a duplication, it is displayed in the UI already
        setTitleHintLabel(element?.tags?.let { getLocationLabel(it, resources) })
        setMarkerIcon(R.drawable.ic_quest_shop)

        featureCtrl = FeatureViewController(featureDictionary, binding.featureTextView, binding.featureIconView)
        featureCtrl.countryOrSubdivisionCode = countryOrSubdivisionCode
        featureCtrl.feature = feature

        binding.featureView.setOnClickListener {
            SearchFeaturesDialog(
                requireContext(),
                featureDictionary,
                element?.geometryType,
                countryOrSubdivisionCode,
                featureCtrl.feature?.name,
                ::filterOnlyShops,
                ::onSelectedFeature
            ).show()
        }

        names = createLocalizedNames(element?.tags.orEmpty()).orEmpty()

        val persistedNames = savedInstanceState?.getString(LOCALIZED_NAMES_DATA)?.let { Json.decodeFromString<List<LocalizedName>>(it) }

        val selectableLanguages = (countryInfo.officialLanguages + countryInfo.additionalStreetsignLanguages).distinct().toMutableList()
        val preferredLanguage = prefs.getString(PREFERRED_LANGUAGE_FOR_NAMES, null)
        if (preferredLanguage != null) {
            if (selectableLanguages.remove(preferredLanguage)) {
                selectableLanguages.add(0, preferredLanguage)
            }
        }

        val adapter = LocalizedNameAdapter(
            persistedNames ?: names.map { it.copy() },
            requireContext(),
            selectableLanguages,
            null,
            null,
            binding.nameContainer.addLanguageButton
        )
        adapter.addOnNameChangedListener { checkIsFormComplete() }
        adapter.registerAdapterDataObserver(AdapterDataChangedWatcher { checkIsFormComplete() })
        lifecycle.addObserver(adapter)
        this.adapter = adapter
        binding.nameContainer.namesList.adapter = adapter
        binding.nameContainer.namesList.isNestedScrollingEnabled = false

        updateNameContainerVisibility()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter?.names?.let { outState.putString(LOCALIZED_NAMES_DATA, Json.encodeToString(it)) }
    }

    private fun filterOnlyShops(feature: Feature): Boolean {
        val fakeElement = Node(-1L, LatLon(0.0, 0.0), feature.tags, 0)
        return IS_SHOP_OR_DISUSED_SHOP_EXPRESSION.matches(fakeElement)
    }

    private fun onSelectedFeature(feature: Feature) {
        featureCtrl.feature = feature
        // clear (previous) names if selected feature contains already a name (i.e. is a brand feature)
        // or is vacant
        if (feature.addTags?.get("name") != null || feature.id == "shop/vacant") {
            adapter?.names = emptyList()
        }

        updateNameContainerVisibility()
        checkIsFormComplete()
    }

    private fun setVacant() {
        onSelectedFeature(createVacantShop(requireContext().resources))
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

    override fun hasChanges(): Boolean =
        feature != featureCtrl.feature || names != adapter?.names

    override fun isFormComplete(): Boolean =
        featureCtrl.feature != null // name is not necessary

    override fun onClickOk() {
        val firstLanguage = adapter?.names?.firstOrNull()?.languageTag?.takeIf { it.isNotBlank() }
        if (firstLanguage != null) prefs.edit { putString(PREFERRED_LANGUAGE_FOR_NAMES, firstLanguage) }

        viewLifecycleScope.launch {
            applyEdit(createEditAction(
                element, geometry,
                adapter?.names.orEmpty(), names,
                featureCtrl.feature!!, feature,
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
    }
}

private suspend fun createEditAction(
    element: Element?,
    geometry: ElementGeometry,
    newNames: List<LocalizedName>,
    previousNames: List<LocalizedName>,
    newFeature: Feature,
    previousFeature: Feature?,
    confirmReplaceShop: suspend () -> Boolean
): ElementEditAction {
    val tagChanges = StringMapChangesBuilder(element?.tags ?: emptyMap())

    if (element != null) {
        /* Do not replace shop if:
           + only a name was added (name was missing before; user wouldn't be able to answer if
             the place changed or not anyway, so rather keep previous information)
           + only the feature was changed but the non-empty name did not change (if it was a
             different shop now, it would also have a different name)

           Ask whether it is still the same shop if:
           + the name was changed
           + the feature was changed and the name was empty before

           Always replace shop if:
           + the feature now or previous feature is a brand feature (i.e. it also overwrites the name)
         */
        val hasAddedNames = previousNames.isEmpty() && newNames.isNotEmpty()
        val hasChangedNames = previousNames != newNames
        val hasChangedFeature = newFeature != previousFeature
        val isFeatureWithName = newFeature.addTags?.get("name") != null
        val wasFeatureWithName = previousFeature?.addTags?.get("name") != null

        val doReplaceShop =
            if (hasAddedNames && !hasChangedFeature
                || hasChangedFeature && !hasChangedNames && previousNames.isNotEmpty()
            ) false
            else if (isFeatureWithName || wasFeatureWithName) true
            else confirmReplaceShop()

        if (doReplaceShop) {
            tagChanges.replaceShop(newFeature.addTags)
        } else {
            for ((key, value) in previousFeature?.removeTags.orEmpty()) {
                tagChanges.remove(key)
            }
            for ((key, value) in newFeature.addTags) {
                tagChanges[key] = value
            }
        }
    }

    newNames.applyTo(tagChanges)
    if (tagChanges["name"] != null) {
        tagChanges.remove("noname")
    }

    return if (element != null) {
        UpdateElementTagsAction(tagChanges.create())
    } else {
        CreateNodeAction(geometry.center, tagChanges)
    }
}

private fun createVacantShop(resources: Resources) = DummyFeature(
    "shop/vacant",
    resources.getString(R.string.vacant_shop_title),
    "maki-shop",
    mapOf("disused:shop" to "yes")
)
