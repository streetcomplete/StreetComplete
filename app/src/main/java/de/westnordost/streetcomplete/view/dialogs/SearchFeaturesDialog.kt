package de.westnordost.streetcomplete.view.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.databinding.ViewFeatureBinding
import de.westnordost.streetcomplete.databinding.ViewSelectPresetBinding
import de.westnordost.streetcomplete.util.getLocalesForFeatureDictionary
import de.westnordost.streetcomplete.util.ktx.hideKeyboard
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.ListAdapter
import de.westnordost.streetcomplete.view.controller.FeatureViewController

/** Search and select a preset */
class SearchFeaturesDialog(
    context: Context,
    private val featureDictionary: FeatureDictionary,
    private val geometryType: GeometryType? = null,
    private val countryOrSubdivisionCode: String? = null,
    text: String? = null,
    private val filterFn: (Feature) -> Boolean = { true },
    private val onSelectedFeatureFn: (Feature) -> Unit,
    private val dismissKeyboardOnClose: Boolean = false,
) : AlertDialog(context) {

    private val binding = ViewSelectPresetBinding.inflate(LayoutInflater.from(context))
    private val locales = getLocalesForFeatureDictionary(context.resources.configuration)
    private val adapter = FeaturesAdapter()

    private val searchText: String? get() = binding.searchEditText.nonBlankTextOrNull

    private val defaultFeatures: List<Feature> by lazy {
        listOf(
            // ordered by usage number according to taginfo
            "amenity/restaurant",
            "shop/convenience",
            "amenity/cafe",
            "shop/supermarket",
            "amenity/fast_food",
            "amenity/pharmacy",
            "shop/clothes",
            "shop/hairdresser"
        ).mapNotNull {
            featureDictionary
                .byId(it)
                .forLocale(*locales)
                .inCountry(countryOrSubdivisionCode).get()
        }
    }

    init {
        binding.searchEditText.setText(text)
        binding.searchEditText.selectAll()
        binding.searchEditText.requestFocus()
        binding.searchEditText.doAfterTextChanged { updateSearchResults() }

        binding.searchResultsList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.searchResultsList.adapter = adapter
        binding.searchResultsList.isNestedScrollingEnabled = true

        setView(binding.root)

        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        updateSearchResults()
    }

    private fun getFeatures(startsWith: String): List<Feature> =
        featureDictionary
            .byTerm(startsWith)
            .forGeometry(geometryType)
            .inCountry(countryOrSubdivisionCode)
            .forLocale(*locales)
            .find()
            .filter(filterFn)

    private fun updateSearchResults() {
        val text = searchText
        val list = if (text == null) defaultFeatures else getFeatures(text)
        adapter.list = list.toMutableList()
        binding.noResultsText.isGone = list.isNotEmpty()
    }

    override fun dismiss() {
        if (dismissKeyboardOnClose) {
            // Handle keyboard not being automatically dismissed on all Android versions. Has to be
            // called before the super method, otherwise it won't work.
            binding.searchEditText.hideKeyboard()
        }
        super.dismiss()
    }

    private inner class FeaturesAdapter : ListAdapter<Feature>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(ViewFeatureBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        inner class ViewHolder(val binding: ViewFeatureBinding) : ListAdapter.ViewHolder<Feature>(binding) {
            private val viewCtrl: FeatureViewController

            init {
                viewCtrl = FeatureViewController(featureDictionary, binding.textView, binding.iconView)
                viewCtrl.countryOrSubdivisionCode = countryOrSubdivisionCode
            }

            override fun onBind(with: Feature) {
                binding.root.setOnClickListener {
                    onSelectedFeatureFn(with)
                    dismiss()
                }
                viewCtrl.searchText = searchText
                viewCtrl.feature = with
            }
        }
    }
}
