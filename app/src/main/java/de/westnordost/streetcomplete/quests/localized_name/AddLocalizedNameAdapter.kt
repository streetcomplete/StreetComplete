package de.westnordost.streetcomplete.quests.localized_name

import android.content.Context
import android.graphics.Typeface
import android.os.AsyncTask
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.Locale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.Abbreviations
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.util.DefaultTextWatcher

import android.view.Menu.NONE
import de.westnordost.streetcomplete.view.AutoCorrectAbbreviationsEditText

/** Carries the data language code + name in that language  */
data class LocalizedName(var languageCode: String, var name: String)

class AddLocalizedNameAdapter(
    initialLocalizedNames: List<LocalizedName>,
    private val context: Context,
    private val languages: List<String>,
    private val abbreviationsByLocale: AbbreviationsByLocale?,
    private val localizedNameSuggestions: List<MutableMap<String, String>>?,
    private val addLanguageButton: View
) : RecyclerView.Adapter<AddLocalizedNameAdapter.ViewHolder>() {

    var localizedNames: MutableList<LocalizedName>
        private set
    private val listeners = mutableListOf<(LocalizedName) -> Unit>()

    init {
        localizedNames = initialLocalizedNames.toMutableList()
        if (localizedNames.isEmpty()) {
            localizedNames.add(LocalizedName(languages[0], ""))
        }
        putDefaultLocalizedNameSuggestion()
        addLanguageButton.setOnClickListener { v ->
            showLanguageSelectMenu(v, getNotAddedLanguages()) { add(it) }
        }

        updateAddLanguageButtonVisibility()
    }

    private fun getNotAddedLanguages(): List<String> {
        val result = languages.toMutableList()
        for (localizedName in localizedNames) {
            result.remove(localizedName.languageCode)
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
        val defaultLanguage = languages[0]
        if (localizedNameSuggestions != null) {
            for (names in localizedNameSuggestions) {
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
        return ViewHolder(inflater.inflate(R.layout.quest_localizedname_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.update(position, localizedNames[position])
    }

    override fun getItemCount() = localizedNames.size

    private fun updateAddLanguageButtonVisibility() {
        addLanguageButton.visibility = if (getNotAddedLanguages().isEmpty()) View.GONE else View.VISIBLE
    }

    private fun remove(index: Int) {
        if (index < 1) return
        localizedNames.removeAt(index)
        notifyItemRemoved(index)

        updateAddLanguageButtonVisibility()
    }

    private fun add(languageCode: String) {
        val insertIndex = itemCount
        localizedNames.add(LocalizedName(languageCode, ""))
        notifyItemInserted(insertIndex)

        updateAddLanguageButtonVisibility()
    }

    /** Show a context menu above the given [view] where the user can select one language from the
     * [languageList], which will be passed to the [callback] */
    private fun showLanguageSelectMenu(
        view: View,
        languageList: List<String>,
        callback: (String) -> Unit
    ) {
        if (languageList.isEmpty()) return

        val popup = PopupMenu(context, view)
        for ((i, languageCode) in languageList.withIndex()) {
            popup.menu.add(NONE, i, NONE, getLanguageMenuItemTitle(languageCode))
        }

        popup.setOnMenuItemClickListener { item ->
            callback(languageList[item.itemId])
            true
        }
        popup.show()
    }

    private fun getLanguageMenuItemTitle(languageCode: String): String {
        if (languageCode.isEmpty()) return context.getString(R.string.quest_streetName_menuItem_nolanguage)

        val locale = Locale(languageCode)

        val displayLanguage = locale.displayLanguage
        val nativeDisplayLanguage = locale.getDisplayLanguage(locale)
        return if (displayLanguage == nativeDisplayLanguage) {
            String.format(
                context.getString(R.string.quest_streetName_menuItem_language_simple),
                languageCode, displayLanguage
            )
        } else {
            String.format(
                context.getString(R.string.quest_streetName_menuItem_language_native),
                languageCode, nativeDisplayLanguage, displayLanguage
            )
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

    private fun getLocalizedNameSuggestionsByLanguageCode(languageCode: String): Map<String, Map<String, String>> {
        val localizedNameSuggestionsMap = mutableMapOf<String, Map<String,String>>()
        if (localizedNameSuggestions != null) {
            for (localizedNameSuggestion in localizedNameSuggestions) {
                val name = localizedNameSuggestion[languageCode] ?: continue

                // "unspecified language" suggestions
                if (languageCode.isEmpty()) {
                    var defaultNameOccurances = 0
                    for (other in localizedNameSuggestion.values) {
                        if (name == other) defaultNameOccurances++
                    }
                    // name=A, name:de=A -> do not consider "A" for "unspecified language" suggestion
                    if (defaultNameOccurances >= 2) continue
                    // only for name=A, name:de=B, name:en=C,...
                }
                localizedNameSuggestionsMap[name] = localizedNameSuggestion
            }
        }
        return localizedNameSuggestionsMap
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var localizedName: LocalizedName

        private val autoCorrectInput : AutoCorrectAbbreviationsEditText = itemView.findViewById(R.id.autoCorrectInput)
        private val buttonLanguage : TextView = itemView.findViewById(R.id.languageButton)
        private val buttonDelete : View = itemView.findViewById(R.id.deleteButton)
        private val buttonNameSuggestions : View = itemView.findViewById(R.id.nameSuggestionsButton)

        init {
            autoCorrectInput.addTextChangedListener(object : DefaultTextWatcher() {
                override fun afterTextChanged(s: Editable) {
                    val name = s.toString()
                    localizedName.name = name
                    if (name.isEmpty()) {
                        val hasSuggestions = !getLocalizedNameSuggestionsByLanguageCode(localizedName.languageCode).isEmpty()
                        buttonNameSuggestions.visibility = if (hasSuggestions) View.VISIBLE else View.GONE
                    } else {
                        buttonNameSuggestions.visibility = View.GONE
                    }
                    for (listener in listeners) {
                        listener(localizedName)
                    }
                }
            })

            buttonDelete.setOnClickListener {
                // clearing focus is very necessary, otherwise crash
                autoCorrectInput.clearFocus()
                remove(adapterPosition)
            }
        }

        fun update(index: Int, ln: LocalizedName) {
            localizedName = ln

            val isFirst = index == 0

            buttonDelete.visibility = if (isFirst) View.INVISIBLE else View.VISIBLE
            buttonLanguage.visibility = if (languages.size > 1) View.VISIBLE else View.INVISIBLE

            autoCorrectInput.setText(localizedName.name)
            autoCorrectInput.requestFocus()
            buttonLanguage.text = localizedName.languageCode

            // first entry is bold (the first entry is supposed to be the "default language", I
            // hope that comes across to the users like this. Otherwise, a text hint is necessary)
            buttonLanguage.setTypeface(null, if (isFirst) Typeface.BOLD else Typeface.NORMAL)
            autoCorrectInput.setTypeface(null, if (isFirst) Typeface.BOLD else Typeface.NORMAL)

            buttonLanguage.setOnClickListener { v: View ->
                val notAddedLanguages = getNotAddedLanguages().toMutableList()
                // in first entry user may select "unspecified language" to cover cases where
                // the default name is no specific language. I.e. see
                // https://wiki.openstreetmap.org/wiki/Multilingual_names#Sardegna_.28Sardinia.29
                if (isFirst) {
                    notAddedLanguages.add(0, "")
                }

                showLanguageSelectMenu(v, notAddedLanguages) { languageCode ->
                    localizedName.languageCode = languageCode
                    buttonLanguage.text = languageCode
                    updateAddLanguageButtonVisibility()
                    updateNameSuggestions()
                }
            }

            updateNameSuggestions()

            // load abbreviations from file in separate thread
            object : AsyncTask<Void, Void, Abbreviations>() {
                override fun doInBackground(vararg params: Void): Abbreviations? {
                    return abbreviationsByLocale?.get(Locale(localizedName.languageCode))
                }

                override fun onPostExecute(abbreviations: Abbreviations?) {
                    autoCorrectInput.abbreviations = abbreviations
                }
            }.execute()
        }

        private fun updateNameSuggestions() {
            val localizedNameSuggestionsMap = getLocalizedNameSuggestionsByLanguageCode(localizedName.languageCode)

            val nameInputNotEmpty = autoCorrectInput.text.toString().trim().isNotEmpty()
            val hasNoNameSuggestions = localizedNameSuggestionsMap.isEmpty()
            buttonNameSuggestions.visibility =
                if (hasNoNameSuggestions || nameInputNotEmpty) View.GONE else View.VISIBLE

            buttonNameSuggestions.setOnClickListener { v ->
                showNameSuggestionsMenu(v, localizedNameSuggestionsMap) { selection ->
                    localizedNames = selection.toLocalizedNameList()
                    notifyDataSetChanged()
                    updateAddLanguageButtonVisibility()
                }
            }
        }
    }

    /** Turn a map of language code to name into a list of LocalizedName */
    private fun Map<String, String>.toLocalizedNameList(): MutableList<LocalizedName> {
        val result = mutableListOf<LocalizedName>()
        val defaultName = this[""]
        for ((key, value) in this) {
            // put default name first
            // (i.e. name=A, name:en=B, name:de=A -> name:de goes first and name is not shown)
            val localizedName = LocalizedName(key, value)
            if (localizedName.name == defaultName) {
                if (localizedName.languageCode.isNotEmpty()) result.add(0, localizedName)
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
