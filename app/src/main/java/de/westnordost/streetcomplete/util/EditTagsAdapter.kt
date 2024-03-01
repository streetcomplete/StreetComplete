package de.westnordost.streetcomplete.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.RowEditTagBinding
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.spToPx
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.serialization.json.Json

// use displaySet and dataSet: displaySet is the sorted map.toList
// editing the map directly is not so simple, because the order may change if the key is changed
class EditTagsAdapter(
    private val displaySet: MutableList<Pair<String, String>>,
    private val dataSet: MutableMap<String, String>,
    private val geometryType: GeometryType, // todo: currently unused, but should be used (later) for getting correct suggestions
    private val featureDictionary: FeatureDictionary,
    context: Context,
    private val prefs: SharedPreferences,
    private val onDataChanged: () -> Unit
) :
    RecyclerView.Adapter<EditTagsAdapter.ViewHolder>() {
    val suggestionHeight = TypedValue().apply { context.theme.resolveAttribute(android.R.attr.listPreferredItemHeight, this, false) }
        .getDimension(context.resources.displayMetrics).toInt()
    val suggestionMaxHeight = (context.resources.displayMetrics.heightPixels * 0.8).toInt()
    // used to avoid covering keyboard button by tag dropdown
    // autocomplete view height is sth like 18sp text size, 16sp edit date text size + some padding
    val keyViewOffset = (context.spToPx(34) + context.dpToPx(32)).toInt()
    val topMargin = context.dpToPx(60).toInt() // for id/editorContainer

    init {
        if (keySuggestionsForFeatureId.isEmpty() && valueSuggestionsByKey.isEmpty()) {
            try {
                val keySuggestions = context.resources.assets.open("tag_editor/keySuggestionsForFeature.json").reader().readText()
                val valueSuggestions = context.resources.assets.open("tag_editor/valueSuggestionsByKey.json").reader().readText()
                // filling maps twice is a bit inefficient, but there are so many duplicate strings that interning is worth it
                Json.decodeFromString<Map<String, Pair<List<String>?, List<String>?>>>(keySuggestions).forEach {
                    keySuggestionsForFeatureId[it.key.intern()] = it.value.first?.map { it.intern() } to it.value.second?.map { it.intern() }
                }
                Json.decodeFromString<Map<String, List<String>>>(valueSuggestions).forEach {
                    valueSuggestionsByKey[it.key.intern()] = it.value.map { it.intern() }
                }
            } catch (e: Exception) {
                Log.w("EditTagsAdapter", "failed to read and parse suggestions: ${e.message}")
            }
        }
    }

    inner class ViewHolder(binding: RowEditTagBinding) : RecyclerView.ViewHolder(binding.root) {
        private fun storeRecentlyUsed(text: String, name: String, isKey: Boolean) { // will be value if not key
            val keys = linkedSetOf(text)
            val pref = "EditTagsAdapter_${name}_" + if (isKey) "keys" else "values"
            keys.addAll(prefs.getString(pref, "")!!.split("§§"))
            prefs.edit { putString(pref, keys.filter { it.isNotEmpty() }.take(15).joinToString("§§")) }
        }

        val keyView: AutoCompleteTextView = binding.keyText.apply {
            var lastFeature: Feature? = null
            val lastSuggestions = linkedSetOf<String>()
            setOnFocusChangeListener { _, focused ->
                val text = text.toString()
                if (focused) setText(text) // to get fresh suggestions and show dropdown; showDropDown() not helping here
                else if (text !in lastSuggestions && text.isNotBlank()) {
                    // store most recently used keys on focus loss
                    //  this may be because user typed answer instead of tapping suggestion
                    //  unfortunately this also happens in other cases where storing may not be wanted, but leave it for now
                    storeRecentlyUsed(text, lastFeature?.id.toString(), true)
                }
            }
            onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                storeRecentlyUsed(text.toString(), lastFeature?.id.toString(), true)
                // move to value view if key is selected from suggestions
                valueView.requestFocus()
            }
            setAdapter(SearchAdapter(context, { search ->
                if (!isFocused) return@SearchAdapter emptyList() // don't search if the field is not focused
                lastFeature = featureDictionary.byTags(dataSet).isSuggestion(false).find().firstOrNull()
                lastSuggestions.clear()
                lastSuggestions.addAll(getKeySuggestions(lastFeature?.id, dataSet).filter { it.startsWith(search) })
                // limit the height of suggestions, because when all are shown the ones on top are hard to reach
                //  top should be suggestionMaxHeight from screen bottom
                // additionally, if the keyboard is not shown, dropdown should not block show keyboard button
                var minus = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val showingKeyboard = WindowInsetsCompat.toWindowInsetsCompat(rootWindowInsets).isVisible(WindowInsetsCompat.Type.ime())
                    dropDownVerticalOffset = if (showingKeyboard) -9 // reading offset that has not been set gives -9 (why?)
                        else keyViewOffset
                    minus = if (showingKeyboard) rootWindowInsets.systemWindowInsetBottom
                        // insets minus offset plus distance of bottom of recycler view from screen bottom
                        // last one needs to consider that view.bottom is relative to parent, which has a 60 dp margin from top
                        else rootWindowInsets.systemWindowInsetBottom - keyViewOffset + ((parent.parent as? RecyclerView)?.let { resources.displayMetrics.heightPixels - topMargin - it.bottom } ?: 0)
                }
                dropDownHeight = (suggestionHeight * lastSuggestions.size).coerceAtMost(suggestionMaxHeight - minus)

                lastSuggestions.toList()
            }, { it }))
            doAfterTextChanged {
                val position = absoluteAdapterPosition
                val newKey = it.toString()
                // do nothing if key is unchanged, happens when views are filled by EditTagsAdapter
                if (displaySet[position].first == newKey) return@doAfterTextChanged
                if (dataSet.containsKey(newKey)) {
                    // don't store duplicate keys, user should rename or delete them
                    context.toast(resources.getString(R.string.tag_editor_duplicate_key, newKey), Toast.LENGTH_LONG)
                    return@doAfterTextChanged
                }
                val oldEntry = displaySet[position]
                dataSet.remove(oldEntry.first)
                val newEntry = newKey to oldEntry.second
                dataSet[newEntry.first] = newEntry.second
                displaySet[position] = newEntry
                onDataChanged()
            }
        }

        val valueView: AutoCompleteTextView = binding.valueText.apply {
            val lastSuggestions = linkedSetOf<String>()
            setOnFocusChangeListener { _, focused ->
                val text = text.toString()
                if (focused) setText(text) // to get fresh suggestions and show dropdown; showDropDown() not helping here
                else if (text !in lastSuggestions && text.isNotBlank() && keyView.text.toString().isNotBlank()) {
                    // store most recently used values on focus loss (user typed answer instead of tapping suggestion)
                    storeRecentlyUsed(text, keyView.text.toString(), false)
                }
            }
            onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                storeRecentlyUsed(text.toString(), keyView.text.toString(), false)
            }
            setAdapter(SearchAdapter(context, { search ->
                if (!isFocused) return@SearchAdapter emptyList()
                val key = displaySet[absoluteAdapterPosition].first
                lastSuggestions.clear()
                prefs.getString("EditTagsAdapter_${keyView.text}_values", "")!!
                    .split("§§").forEach {
                        if (it.startsWith(search) && it.isNotEmpty())
                            lastSuggestions.add(it)
                    }
                lastSuggestions.addAll(valueSuggestionsByKey[key].orEmpty().filter { it.startsWith(search) })
                val minus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) rootWindowInsets.systemWindowInsetBottom
                    else 0
                dropDownHeight = (suggestionHeight * lastSuggestions.size).coerceAtMost(suggestionMaxHeight - minus)
                lastSuggestions.toList()
            }, { it }))
            doAfterTextChanged {
                val position = absoluteAdapterPosition
                if (displaySet[position].second == it.toString()) return@doAfterTextChanged
                val oldEntry = displaySet[position]
                val newEntry = oldEntry.first to it.toString()
                dataSet[newEntry.first] = newEntry.second
                displaySet[position] = newEntry
                onDataChanged()
            }
        }

        val delete: ImageView = binding.deleteButton.apply {
            setOnClickListener {
                val position = absoluteAdapterPosition
                val oldEntry = displaySet[position]
                if (oldEntry.second.isEmpty()) {
                    // delete row if value is empty
                    displaySet.removeAt(position)
                    dataSet.remove(oldEntry.first)
                } else {
                    // otherwise clear value only
                    displaySet[position] = displaySet[position].copy(second = "")
                    dataSet[oldEntry.first] = ""
                    // show suggestions if not entering a name
                    if (!oldEntry.second.startsWith("name"))
                        valueView.postDelayed({ valueView.requestFocus() }, 10)
                }
                notifyDataSetChanged() // slightly weird behavior if only notifying about the actual changes
                onDataChanged()
            }
            setOnLongClickListener {
                val position = absoluteAdapterPosition
                val oldEntry = displaySet[position]
                displaySet.removeAt(position)
                dataSet.remove(oldEntry.first)
                onDataChanged()
                notifyDataSetChanged()
                true
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RowEditTagBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.keyView.setText(displaySet[position].first)
        viewHolder.valueView.setText(displaySet[position].second)
    }

    override fun getItemCount() = displaySet.size

    override fun getItemId(position: Int) = position.toLong()

    // todo: use geometry type for suggestions
    //  means that generateTagSuggestions needs to be adjusted to generate something containing allowed geometry types
    //  and maybe suggestions should only be for a single geometry type? or for all geometry types for which this key is allowed?
    //  basic test: no building suggestion when adding a shop node (though currently this is manually excluded)
    //  ideally FeatureDictionary at some point implements fields / moreFields...
    private fun getKeySuggestions(featureId: String?, tags: Map<String, String>): Collection<String> {
        val suggestions = prefs.getString("EditTagsAdapter_${featureId}_keys", "")!!.split("§§").filter { it.isNotEmpty() }.toMutableSet()
        if (featureId == null) return suggestions.filterNot { it in tags.keys }
        val fields = getMainSuggestions(featureId)
        val moreFields = getSecondarySuggestions(featureId)
        val fieldSuggestions = mutableListOf<String>()
        val moreFieldSuggestions = mutableListOf<String>()
        fields.forEach {
            if (it == "building" || it.startsWith("gnis:feature_id") ) return@forEach
            if (it.startsWith('{')) // does this actually trigger? or is it unnecessary?
                fieldSuggestions.addAll(getMainSuggestions(it.substringAfter('{').substringBefore('}')))
            else fieldSuggestions.add(it)
        }
        moreFields.forEach {
            // ignore some moreFields that often are inappropriate (but keep if in fields!)
            if (it.startsWith("ref:") || it.startsWith("building") || it == "gnis:feature_id" || it == "ele" || it == "height" ) return@forEach
            if (it.startsWith('{'))
                moreFieldSuggestions.addAll(getSecondarySuggestions(it.substringAfter('{').substringBefore('}')))
            else moreFieldSuggestions.add(it)
        }

        // suggestions should not be cluttered with all those address tags, but we don't want to ignore them completely
        // but we want to ignore some refs, and building which shows up for shops, but is usually not a good idea because we ignore geometry
        val fieldsMoveToEnd = fieldSuggestions.filter { it.startsWith("addr:") || it.startsWith("ref:") || it.startsWith("tiger:") }
        fieldSuggestions.removeAll(fieldsMoveToEnd)
        fieldSuggestions.removeAll { it.startsWith("{") } // appeared in the latest presets update, maybe we should actually go deeper for this '{'?
        val moreFieldsMoveToEnd = moreFieldSuggestions.filter { it.startsWith("addr:") || it.startsWith("ref:") || it.startsWith("tiger:") }
        moreFieldSuggestions.removeAll(moreFieldsMoveToEnd)
        moreFieldSuggestions.removeAll { it.startsWith("{") }

        // order: previously entered values, fields, moreFields, addr fields, addr moreFields
        // do it in this complicated way because we don't want to (re)move keys the user has entered
        suggestions.addAll(fieldSuggestions)
        suggestions.addAll(moreFieldSuggestions)
        suggestions.addAll(fieldsMoveToEnd)
        suggestions.addAll(moreFieldsMoveToEnd)

        suggestions.removeAll(tags.keys) // don't suggest what we already have
        return suggestions
        // can be optimized, but likely not worth the work
    }

    private fun getMainSuggestions(featureId: String): List<String> {
        val suggestions = keySuggestionsForFeatureId[featureId]?.first
        return suggestions ?: if (featureId.contains('/')) getMainSuggestions(featureId.substringBeforeLast('/')) else emptyList()
    }

    private fun getSecondarySuggestions(featureId: String): List<String> {
        val suggestions = keySuggestionsForFeatureId[featureId]?.second
        return suggestions ?: if (featureId.contains('/')) getSecondarySuggestions(featureId.substringBeforeLast('/')) else emptyList()
    }

    companion object {
        private val keySuggestionsForFeatureId = hashMapOf<String, Pair<List<String>?, List<String>?>>()
        private val valueSuggestionsByKey = hashMapOf<String, List<String>>()
    }
}
