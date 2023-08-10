package de.westnordost.streetcomplete.quests

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.Menu.NONE
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.view.controller.AutoCorrectAbbreviationsViewController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LocalizedNameAdapter(
    initialNames: List<LocalizedName>,
    private val context: Context,
    private val languageTags: List<String>,
    private val abbreviationsByLocale: AbbreviationsByLocale?,
    namesSuggestions: List<List<LocalizedName>>?,
    private val addLanguageButton: View,
    private val rowLayoutResId: Int = R.layout.row_localizedname
) : RecyclerView.Adapter<LocalizedNameAdapter.ViewHolder>(), DefaultLifecycleObserver {

    var names: List<LocalizedName>
        get() = _names.filter { it.name.isNotEmpty() }
        set(value) {
            _names = value.toMutableList()
            if (_names.isEmpty()) {
                val defaultLanguage = languageTags[0]
                _names.add(LocalizedName(defaultLanguage, ""))
            }
            notifyDataSetChanged()
        }

    var emptyNamesHint: String? = null
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    private var _names: MutableList<LocalizedName> = mutableListOf()
    private val listeners = mutableListOf<(LocalizedName) -> Unit>()

    /** list of maps consisting of key = language tag, value = name */
    private val nameSuggestions: List<MutableMap<String, String>>?

    private val viewLifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        names = initialNames

        this.nameSuggestions = namesSuggestions?.map { localizedNames ->
            localizedNames.associate { it.languageTag to it.name }.toMutableMap()
        }
        putDefaultLocalizedNameSuggestion()

        addLanguageButton.setOnClickListener { v ->
            showLanguageSelectMenu(v, getNotAddedLanguageTags()) { add(it) }
        }

        updateAddLanguageButtonVisibility()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        viewLifecycleScope.cancel()
    }

    private fun getNotAddedLanguageTags(): List<String> {
        if (languageTags.size == 1) return emptyList()

        val result = languageTags.toMutableList()
        for (localizedName in _names) {
            result.remove(localizedName.languageTag)
        }
        return result
    }

    fun addOnNameChangedListener(listener: (LocalizedName) -> Unit) {
        listeners.add(listener)
    }

    /* Names are usually specified without language information (name=My Street). To provide
     * meaningful name suggestions per language, it must then be determined in which language this
     * name tag is. */
    private fun putDefaultLocalizedNameSuggestion() {
        val defaultLanguage = languageTags[0]
        if (nameSuggestions != null) {
            for (localizedNames in nameSuggestions) {
                val names = localizedNames
                val defaultName = names[""]
                if (defaultName != null) {
                    // name=A -> name=A, name:de=A (in Germany)
                    if (!names.containsKey(defaultLanguage)) {
                        names[defaultLanguage] = defaultName
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(rowLayoutResId, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.update(position, _names[position])
    }

    override fun getItemCount() = _names.size

    private fun updateAddLanguageButtonVisibility() {
        addLanguageButton.isGone = getNotAddedLanguageTags().isEmpty()
    }

    private fun remove(index: Int) {
        if (index < 1) return
        _names.removeAt(index)
        notifyItemRemoved(index)

        updateAddLanguageButtonVisibility()
    }

    private fun add(languageTag: String) {
        val insertIndex = itemCount
        _names.add(LocalizedName(languageTag, ""))
        notifyItemInserted(insertIndex)

        updateAddLanguageButtonVisibility()
    }

    /** Show a context menu above the given [view] where the user can select one language from the
     * [languageTagList], which will be passed to the [callback] */
    private fun showLanguageSelectMenu(
        view: View,
        languageTagList: List<String>,
        callback: (String) -> Unit
    ) {
        if (languageTagList.isEmpty()) return

        val popup = PopupMenu(context, view)
        for ((i, languageTag) in languageTagList.withIndex()) {
            popup.menu.add(NONE, i, NONE, getLanguageMenuItemTitle(languageTag))
        }

        popup.setOnMenuItemClickListener { item ->
            callback(languageTagList[item.itemId])
            true
        }
        popup.show()
    }

    private fun getLanguageMenuItemTitle(languageTag: String): String {
        if (languageTag.isEmpty()) return context.getString(R.string.quest_streetName_menuItem_nolanguage)
        if (languageTag == "international") return context.getString(R.string.quest_streetName_menuItem_international)
        val isRomanization = languageTag.endsWith("Latn")
        val locale = Locale.forLanguageTag(languageTag)

        val displayLanguage = locale.displayLanguage
        val nativeDisplayLanguage = locale.getDisplayLanguage(locale)
        return if (!isRomanization) {
            if (displayLanguage == nativeDisplayLanguage) {
                String.format(
                    context.getString(R.string.quest_streetName_menuItem_language_simple),
                    languageTag, displayLanguage
                )
            } else {
                String.format(
                    context.getString(R.string.quest_streetName_menuItem_language_native),
                    languageTag, nativeDisplayLanguage, displayLanguage
                )
            }
        } else {
            if (displayLanguage == nativeDisplayLanguage) {
                String.format(
                    context.getString(R.string.quest_streetName_menuItem_language_with_script_simple),
                    languageTag, displayLanguage, locale.displayScript
                )
            } else {
                String.format(
                    context.getString(R.string.quest_streetName_menuItem_language_with_script_native),
                    languageTag, nativeDisplayLanguage, displayLanguage, locale.displayScript
                )
            }
        }
    }

    /** Show a context menu above the given [view] where the user can select one key from the
     * [localizedNameSuggestionsMap]. The value of the selected key will be passed to the
     * [callback] */
    private fun showNameSuggestionsMenu(
        view: View,
        localizedNameSuggestionsMap: Map<String, Map<String, String>>,
        callback: (Map<String, String>) -> Unit
    ) {
        val popup = PopupMenu(context, view)

        for ((i, key) in localizedNameSuggestionsMap.keys.withIndex()) {
            popup.menu.add(NONE, i, NONE, key)
        }

        popup.setOnMenuItemClickListener { item ->
            val selected = localizedNameSuggestionsMap[item.title.toString()]
            callback(selected!!)
            true
        }
        popup.show()
    }

    /** returns map of the name in the given language to map of language to name ??*/
    private fun getLocalizedNameSuggestionsByLanguageTag(languageTag: String): Map<String, Map<String, String>> {
        val localizedNameSuggestionsMap = mutableMapOf<String, Map<String, String>>()
        if (nameSuggestions != null) {
            for (namesByLanguageTag in nameSuggestions) {
                val name = namesByLanguageTag[languageTag] ?: continue

                // "unspecified language" suggestions
                if (languageTag.isEmpty()) {
                    var defaultNameOccurrences = 0
                    for (other in namesByLanguageTag.values) {
                        if (name == other) defaultNameOccurrences++
                    }
                    // name=A, name:de=A -> do not consider "A" for "unspecified language" suggestion
                    if (defaultNameOccurrences >= 2) continue
                    // only for name=A, name:de=B, name:en=C,...
                }
                localizedNameSuggestionsMap[name] = namesByLanguageTag
            }
        }
        return localizedNameSuggestionsMap
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var localizedName: LocalizedName

        private val input: EditText = itemView.findViewById(R.id.autoCorrectInput)
        private val buttonLanguage: TextView = itemView.findViewById(R.id.languageButton)
        private val buttonDelete: View = itemView.findViewById(R.id.deleteButton)
        private val buttonNameSuggestions: View = itemView.findViewById(R.id.nameSuggestionsButton)

        private val autoCorrectAbbreviations = AutoCorrectAbbreviationsViewController(input)

        init {
            input.doAfterTextChanged {
                val name = input.text.toString()
                localizedName.name = name.trim()
                buttonNameSuggestions.isGone = name.isNotEmpty()
                    || getLocalizedNameSuggestionsByLanguageTag(localizedName.languageTag).isEmpty()
                for (listener in listeners) {
                    listener(localizedName)
                }
            }

            buttonDelete.setOnClickListener {
                // clearing focus is very necessary, otherwise crash
                input.clearFocus()
                remove(adapterPosition)
            }
        }

        fun update(index: Int, ln: LocalizedName) {
            localizedName = ln

            val isFirst = index == 0

            buttonDelete.isInvisible = isFirst
            buttonLanguage.isEnabled = languageTags.size > 1

            if (isFirst) {
                input.hint = emptyNamesHint
                if (emptyNamesHint == null) input.requestFocus()
            }
            input.setText(localizedName.name)
            updateLanguage(localizedName.languageTag)

            // first entry is bold (the first entry is supposed to be the "default language", I
            // hope that comes across to the users like this. Otherwise, a text hint is necessary)
            buttonLanguage.setTypeface(null, if (isFirst) Typeface.BOLD else Typeface.NORMAL)
            input.setTypeface(null, if (isFirst) Typeface.BOLD else Typeface.NORMAL)

            buttonLanguage.setOnClickListener { v: View ->
                val notAddedLanguageTags = getNotAddedLanguageTags().toMutableList()
                // in first entry user may select "unspecified language" to cover cases where
                // the default name is no specific language. I.e. see
                // https://wiki.openstreetmap.org/wiki/Multilingual_names#Sardegna_.28Sardinia.29
                if (isFirst) {
                    notAddedLanguageTags.add(0, "")
                }

                showLanguageSelectMenu(v, notAddedLanguageTags) { languageTag ->
                    localizedName.languageTag = languageTag
                    updateLanguage(languageTag)
                    updateAddLanguageButtonVisibility()
                }
            }
        }

        private fun updateLanguage(languageTag: String) {
            buttonLanguage.text = if (languageTag == "international") "🌍" else languageTag
            updateNameSuggestions()
            updateAbbreviations()
        }

        private fun updateNameSuggestions() {
            val localizedNameSuggestionsMap = getLocalizedNameSuggestionsByLanguageTag(localizedName.languageTag)

            val nameInputEmpty = input.text.toString().trim().isEmpty()
            val hasNameSuggestions = localizedNameSuggestionsMap.isNotEmpty()
            buttonNameSuggestions.isGone = !nameInputEmpty || !hasNameSuggestions

            buttonNameSuggestions.setOnClickListener { v ->
                showNameSuggestionsMenu(v, localizedNameSuggestionsMap) { selection ->
                    _names = selection.toLocalizedNameList()
                    notifyDataSetChanged()
                    updateAddLanguageButtonVisibility()
                }
            }
        }

        private fun updateAbbreviations() {
            autoCorrectAbbreviations.abbreviations = null
            // load abbreviations from file in background
            viewLifecycleScope.launch {
                autoCorrectAbbreviations.abbreviations = withContext(Dispatchers.IO) {
                    abbreviationsByLocale?.get(Locale(localizedName.languageTag))
                }
            }
        }
    }

    /** Turn a map of language tag to name into a list of LocalizedName */
    private fun Map<String, String>.toLocalizedNameList(): MutableList<LocalizedName> {
        val result = mutableListOf<LocalizedName>()
        val defaultName = this[""]
        for ((key, value) in this) {
            // put default name first
            // (i.e. name=A, name:en=B, name:de=A -> name:de goes first and name is not shown)
            val localizedName = LocalizedName(key, value)
            if (localizedName.name == defaultName) {
                if (localizedName.languageTag.isNotEmpty()) result.add(0, localizedName)
            } else {
                result.add(localizedName)
            }
        }
        // this is for the case: name=A, name:de=B, name:en=C -> name goes first
        if (result[0].name != defaultName && defaultName != null) {
            result.add(0, LocalizedName("", defaultName))
        }
        return result
    }
}
