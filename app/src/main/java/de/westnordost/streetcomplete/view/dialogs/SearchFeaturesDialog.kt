package de.westnordost.streetcomplete.view.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isEmpty
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.russhwolf.settings.ObservableSettings
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.getByLocation
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.databinding.ViewFeatureBinding
import de.westnordost.streetcomplete.databinding.ViewSelectPresetBinding
import de.westnordost.streetcomplete.util.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.ktx.allExceptFirstAndLast
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.hideKeyboard
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.ListAdapter
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.Locale

/** Search and select a preset */
class SearchFeaturesDialog(
    context: Context,
    private val featureDictionary: FeatureDictionary,
    private val geometryType: GeometryType? = null,
    private val countryOrSubdivisionCode: String? = null,
    text: String? = null,
    private val filterFn: (Feature) -> Boolean = { true },
    private val onSelectedFeatureFn: (Feature) -> Unit,
    private val codesOfDefaultFeatures: List<String>,
    private val dismissKeyboardOnClose: Boolean = false,
    private val pos: LatLon? = null
) : AlertDialog(context), KoinComponent {

    private val binding = ViewSelectPresetBinding.inflate(LayoutInflater.from(context))
    private val languages = getLanguagesForFeatureDictionary(context.resources.configuration)
    private val adapter = FeaturesAdapter()
    private val countryInfos: CountryInfos by inject()
    private val countryBoundaries: Lazy<CountryBoundaries> by inject(named("CountryBoundariesLazy"))
    private val prefs: ObservableSettings by inject()

    private val searchText: String? get() = binding.searchEditText.nonBlankTextOrNull

    init {
        binding.searchEditText.setText(text)
        binding.searchEditText.selectAll()
        binding.searchEditText.requestFocus()
        binding.searchEditText.doAfterTextChanged { updateSearchResults() }

        binding.searchResultsList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.searchResultsList.adapter = adapter
        binding.searchResultsList.isNestedScrollingEnabled = true

        setView(binding.root)

        if (prefs.getBoolean(Prefs.CREATE_NODE_SHOW_KEYBOARD, true) || text != null || codesOfDefaultFeatures.isEmpty())
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val params = ViewGroup.LayoutParams(context.resources.dpToPx(58).toInt(), context.resources.dpToPx(58).toInt())
        codesOfDefaultFeatures.forEach {
            val resId = iconOnlyFeatures[it] ?: return@forEach
            val feature = featureDictionary.byId(it).get() ?: return@forEach
            binding.shortcuts.addView(ImageView(context).apply {
                setImageResource(resId)
                layoutParams = params
                setOnClickListener {
                    onSelectedFeatureFn(feature)
                    dismiss()
                }
            })
        }
        if (!binding.shortcuts.isEmpty())
            binding.shortcutScrollView.isVisible = true

        updateSearchResults()
    }

    // todo: this could use some update after SC changes
    private fun getFeatures(startsWith: String): List<Feature> {
        featureDictionary.getByTerm(
            search = startsWith,
            languages = languages,
            country = countryOrSubdivisionCode,
            geometry = geometryType,
        ).filter(filterFn).take(50).toList()
        return if (prefs.getBoolean(Prefs.SEARCH_MORE_LANGUAGES, false)) {
            // even if there are many languages, UI stuff will likely be slower than the multiple searches
            val otherLocales = languages.toList().allExceptFirstAndLast() + // first is default, last is null
                (pos?.let { p ->
                    val c = countryInfos.getByLocation(countryBoundaries.value, p.longitude, p.latitude)
                    c.officialLanguages.map { Locale(it, c.countryCode).toLanguageTag() }
                } ?: emptyList())
            (featureDictionary.getByTerm( // get default results
                    search = startsWith,
                    languages = languages,
                    country = countryOrSubdivisionCode,
                    geometry = geometryType,
                ).filter(filterFn).take(50).toList() +
                otherLocales.toSet().flatMap {
                    if (it == null) return@flatMap emptyList()
                    featureDictionary.getByTerm( // plus results for each additional locale
                        search = startsWith,
                        languages = listOf(it),
                        country = countryOrSubdivisionCode,
                        geometry = geometryType,
                    ).filter(filterFn).take(50).toList()
                }).distinctBy { it.id }
        } else
            featureDictionary.getByTerm(
                search = startsWith,
                languages = languages,
                country = countryOrSubdivisionCode,
                geometry = geometryType,
            ).filter(filterFn).take(50).toList()
    }

    private fun updateSearchResults() {
        val text = searchText
        val list = if (text == null) codesOfDefaultFeatures.filterNot { it in iconOnlyFeatures }.mapNotNull {
            featureDictionary.getById(
                id = it,
                languages = languages,
                country = countryOrSubdivisionCode
            )
        } else
            getFeatures(text)
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

// todo: weird mix of pin icons, quest icons, temaki icons
//  ideally all would be same style, especially avoid monochrome temaki icons
//  the colors really help a lot for finding the right icon very quickly
private val iconOnlyFeatures = mapOf(
    "amenity/bench" to R.drawable.ic_preset_temaki_bench,
    "amenity/lounger" to R.drawable.ic_preset_temaki_lounger,
    "amenity/bicycle_parking" to R.drawable.ic_quest_bicycle_parking,
    "amenity/motorcycle_parking" to R.drawable.ic_quest_motorcycle_parking,
    "leisure/picnic_table" to R.drawable.ic_preset_maki_picnic_site,
    "amenity/waste_basket" to R.drawable.ic_preset_maki_waste_basket,
    "amenity/recycling_container" to R.drawable.ic_quest_recycling_container,
    "amenity/bicycle_repair_station" to R.drawable.ic_quest_bicycle_repair,
    "amenity/drinking_water" to R.drawable.ic_quest_drinking_water,
    "emergency/fire_hydrant" to R.drawable.ic_quest_fire_hydrant,
    "amenity/vending_machine" to R.drawable.ic_preset_temaki_vending_machine,
    "amenity/vending_machine/cigarettes" to R.drawable.ic_preset_temaki_vending_cigarettes,
    "amenity/vending_machine/excrement_bags" to R.drawable.ic_preset_temaki_vending_pet_waste,
    "amenity/vending_machine/public_transport_tickets" to R.drawable.ic_preset_temaki_vending_tickets,
    "amenity/vending_machine/drinks" to R.drawable.ic_preset_temaki_vending_cold_drink,
    "amenity/atm" to R.drawable.ic_quest_money,
    "natural/tree" to R.drawable.ic_quest_tree,
    "tourism/information/guidepost" to R.drawable.ic_quest_street_name,
    "amenity/post_box" to R.drawable.ic_quest_mail,
    "amenity/charging_station" to R.drawable.ic_quest_car_charger,
    "highway/street_lamp" to R.drawable.ic_preset_temaki_street_lamp_arm,
    "man_made/surveillance/camera" to R.drawable.ic_quest_surveillance_camera,
    "highway/speed_camera" to R.drawable.ic_preset_temaki_security_camera,
    "highway/crossing/unmarked" to R.drawable.ic_quest_pedestrian,
    "highway/crossing/uncontrolled" to R.drawable.ic_quest_pedestrian_crossing,
    "highway/crossing/traffic_signals" to R.drawable.ic_quest_blind_traffic_lights_sound,
    "highway/traffic_signals" to R.drawable.ic_quest_traffic_lights,
    "barrier/kerb" to R.drawable.ic_quest_kerb_tactile_paving,
    "barrier/kerb/flush" to R.drawable.ic_preset_temaki_kerb_flush,
    "barrier/kerb/rolled" to R.drawable.ic_preset_temaki_kerb_rolled,
    "barrier/kerb/raised" to R.drawable.ic_preset_temaki_kerb_raised,
    "barrier/kerb/lowered" to R.drawable.ic_preset_temaki_kerb_lowered,
    "barrier/bollard" to R.drawable.ic_preset_temaki_bollard,
    "traffic_calming/table" to R.drawable.ic_preset_temaki_speed_table,
    "traffic_calming/bump" to R.drawable.ic_preset_temaki_speed_bump,
    "entrance" to R.drawable.ic_quest_door,
    "highway/stop" to R.drawable.ic_preset_temaki_stop,
    "highway/give_way" to R.drawable.ic_preset_temaki_yield,
)
