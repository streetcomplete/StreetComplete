package de.westnordost.streetcomplete.view.dialogs

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SearchView.OnQueryTextListener
import androidx.appcompat.app.AlertDialog
import androidx.core.os.ConfigurationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.databinding.ViewSelectPresetBinding
import de.westnordost.streetcomplete.databinding.ViewShopTypeBinding
import de.westnordost.streetcomplete.util.ktx.toList
import de.westnordost.streetcomplete.util.ktx.toTypedArray

/** Search and select a preset */
class SearchPresetDialog(
    context: Context,
    private val featureDictionary: FeatureDictionary,
    private val geometryType: GeometryType?,
    private val countryOrSubdivisionCode: String?,
    private val filter: (Feature) -> Boolean
) : AlertDialog(context) {

    private val binding = ViewSelectPresetBinding.inflate(LayoutInflater.from(context))
    private val locales = ConfigurationCompat.getLocales(context.resources.configuration).toTypedArray()
    private val adapter = PresetsAdapter(context)

    init {
        binding.searchEditText.requestFocus()
        binding.searchEditText.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.items = newText?.trim()?.let { getFeatures(it) } ?: emptyList()
                return false
            }
        })

        binding.searchResultsList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.searchResultsList.adapter = adapter
        binding.searchResultsList.isNestedScrollingEnabled = true
    }


    private fun getFeatures(startsWith: String): List<Feature> =
        featureDictionary
            .byTerm(startsWith)
            .forGeometry(geometryType)
            .inCountry(countryOrSubdivisionCode)
            .forLocale(*locales)
            .find()
            .filter(filter)
}

private class PresetsAdapter(
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<Feature> = emptyList()
    set(value) {
        if (field == value) return
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount() = items.size
}

private fun Feature.getIconDrawable(context: Context): Drawable? {
    if (icon == null) return null
    val resName = "ic_preset_${icon.replace('-','_')}"
    val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
    if (id == 0) return null
    return context.getDrawable(id)
}
