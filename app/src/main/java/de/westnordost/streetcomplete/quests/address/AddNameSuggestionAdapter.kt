package de.westnordost.streetcomplete.quests.address

import android.content.Context
import android.graphics.Typeface
import android.os.AsyncTask
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import java.util.Locale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.Abbreviations
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.util.DefaultTextWatcher

import android.view.Menu.NONE
import de.westnordost.streetcomplete.view.AutoCorrectAbbreviationsEditText

/** Carries the data language code + name in that language  */
data class Name(var languageCode: String, var name: String)

class AddNameSuggestionAdapter(
        initialNames: List<Name>,
        private val context: Context,
        private val languages: List<String>,
        private val abbreviationsByLocale: AbbreviationsByLocale?,
        private val NameSuggestions: List<MutableMap<String, String>>?,
        private val addLanguageButton: Button
) : RecyclerView.Adapter<AddNameSuggestionAdapter.ViewHolder>() {

    var localizedNames: MutableList<Name>
        private set
    private val listeners = mutableListOf<(Name) -> Unit>()

    init {
        localizedNames = initialNames.toMutableList()
        if (localizedNames.isEmpty()) {
            localizedNames.add(Name(languages[0], ""))
        }
        putDefaultNameSuggestion()
        addLanguageButton.setOnClickListener { v ->
            showLanguageSelectMenu(v, getNotAddedLanguages()) { add(it) }
        }

        updateAddLanguageButtonVisibility()
    }

    private fun getNotAddedLanguages(): List<String> {
        val result = languages.toMutableList()
        for (Name in localizedNames) {
            result.remove(Name.languageCode)
        }
        return result
    }

    fun addOnNameChangedListener(listener: (Name) -> Unit) {
        listeners.add(listener)
    }

    /* Names are usually specified without language information (name=My Street). To provide
     * meaningful name suggestions per language, it must then be determined in which language this
     * name tag is. */
    private fun putDefaultNameSuggestion() {
        val defaultLanguage = languages[0]
        if (NameSuggestions != null) {
            for (names in NameSuggestions) {
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
        localizedNames.add(Name(languageCode, ""))
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
     * [NameSuggestionsMap]. The value of the selected key will be passed to the
     * [callback] */
    private fun showNameSuggestionsMenu(
            view: View,
            NameSuggestionsMap: Map<String, Map<String, String>>,
            callback: (Map<String, String>) -> Unit
    ) {
        val popup = PopupMenu(context, view)

        for ((i, key) in NameSuggestionsMap.keys.withIndex()) {
            popup.menu.add(NONE, i, NONE, key)
        }

        popup.setOnMenuItemClickListener { item ->
            val selected = NameSuggestionsMap[item.title.toString()]
            callback(selected!!)
            true
        }
        popup.show()
    }

    private fun getNameSuggestionsByLanguageCode(languageCode: String): Map<String, Map<String, String>> {
        val NameSuggestionsMap = mutableMapOf<String, Map<String,String>>()
        if (NameSuggestions != null) {
            for (NameSuggestion in NameSuggestions) {
                val name = NameSuggestion[languageCode] ?: continue

                // "unspecified language" suggestions
                if (languageCode.isEmpty()) {
                    var defaultNameOccurances = 0
                    for (other in NameSuggestion.values) {
                        if (name == other) defaultNameOccurances++
                    }
                    // name=A, name:de=A -> do not consider "A" for "unspecified language" suggestion
                    if (defaultNameOccurances >= 2) continue
                    // only for name=A, name:de=B, name:en=C,...
                }
                NameSuggestionsMap[name] = NameSuggestion
            }
        }
        return NameSuggestionsMap
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var Name: Name

        private val autoCorrectInput : AutoCorrectAbbreviationsEditText = itemView.findViewById(R.id.autoCorrectInput)
        private val buttonLanguage : TextView = itemView.findViewById(R.id.languageButton)
        private val buttonDelete : TextView = itemView.findViewById(R.id.deleteButton)
        private val buttonNameSuggestions : View = itemView.findViewById(R.id.nameSuggestionsButton)

        init {
            autoCorrectInput.addTextChangedListener(object : DefaultTextWatcher() {
                override fun afterTextChanged(s: Editable) {
                    val name = s.toString()
                    Name.name = name
                    if (name.isEmpty()) {
                        val hasSuggestions = !getNameSuggestionsByLanguageCode(Name.languageCode).isEmpty()
                        buttonNameSuggestions.visibility = if (hasSuggestions) View.VISIBLE else View.GONE
                    } else {
                        buttonNameSuggestions.visibility = View.GONE
                    }
                    for (listener in listeners) {
                        listener(Name)
                    }
                }
            })

            buttonDelete.setOnClickListener {
                // clearing focus is very necessary, otherwise crash
                autoCorrectInput.clearFocus()
                remove(adapterPosition)
            }
        }

        fun update(index: Int, ln: Name) {
            Name = ln

            val isFirst = index == 0

            buttonDelete.visibility = if (isFirst) View.INVISIBLE else View.VISIBLE
            buttonLanguage.visibility = if (languages.size > 1) View.VISIBLE else View.INVISIBLE

            autoCorrectInput.setText(Name.name)
            autoCorrectInput.requestFocus()
            buttonLanguage.text = Name.languageCode

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
                    Name.languageCode = languageCode
                    buttonLanguage.text = languageCode
                    updateAddLanguageButtonVisibility()
                    updateNameSuggestions()
                }
            }

            updateNameSuggestions()

            // load abbreviations from file in separate thread
            object : AsyncTask<Void, Void, Abbreviations>() {
                override fun doInBackground(vararg params: Void): Abbreviations? {
                    return abbreviationsByLocale?.get(Locale(Name.languageCode))
                }

                override fun onPostExecute(abbreviations: Abbreviations?) {
                    autoCorrectInput.abbreviations = abbreviations
                }
            }.execute()
        }

        private fun updateNameSuggestions() {
            val NameSuggestionsMap = getNameSuggestionsByLanguageCode(Name.languageCode)

            val nameInputNotEmpty = autoCorrectInput.text.toString().trim().isNotEmpty()
            val hasNoNameSuggestions = NameSuggestionsMap.isEmpty()
            buttonNameSuggestions.visibility =
                    if (hasNoNameSuggestions || nameInputNotEmpty) View.GONE else View.VISIBLE

            buttonNameSuggestions.setOnClickListener { v ->
                showNameSuggestionsMenu(v, NameSuggestionsMap) { selection ->
                    localizedNames = selection.toNameList()
                    notifyDataSetChanged()
                    updateAddLanguageButtonVisibility()
                }
            }
        }
    }

    /** Turn a map of language code to name into a list of Name */
    private fun Map<String, String>.toNameList(): MutableList<Name> {
        val result = mutableListOf<Name>()
        val defaultName = this[""]
        for ((key, value) in this) {
            // put default name first
            // (i.e. name=A, name:en=B, name:de=A -> name:de goes first and name is not shown)
            val Name = Name(key, value)
            if (Name.name == defaultName) {
                if (Name.languageCode.isNotEmpty()) result.add(0, Name)
            } else {
                result.add(Name)
            }
        }
        // this is for the case: name=A, name:de=B, name:en=C -> name goes first
        if (result[0].name != defaultName && defaultName != null) {
            result.add(0, Name("", defaultName))
        }
        return result
    }
}
