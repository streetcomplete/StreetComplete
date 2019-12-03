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
import android.widget.TextView

import java.util.Locale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.DefaultTextWatcher

import android.view.Menu.NONE
import de.westnordost.streetcomplete.view.AutoCorrectAbbreviationsEditText

/** Carries the data language code + name in that language  */
data class Name(var languageCode: String, var name: String)

class AddNameSuggestionAdapter(
        initialNames: List<Name>,
        private val context: Context,
        private val NameSuggestions: List<MutableMap<String, String>>?
) : RecyclerView.Adapter<AddNameSuggestionAdapter.ViewHolder>() {

    var names: MutableList<Name>
        private set
    private val listeners = mutableListOf<(Name) -> Unit>()

    init {
        names = initialNames.toMutableList()
        if (names.isEmpty()) {
            names.add(Name("dummy", "")) // TODO: eradicate hack
        }
        putDefaultNameSuggestion()
    }

    fun addOnNameChangedListener(listener: (Name) -> Unit) {
        listeners.add(listener)
    }

    /* Names are usually specified without language information (name=My Street). To provide
     * meaningful name suggestions per language, it must then be determined in which language this
     * name tag is. */
    private fun putDefaultNameSuggestion() {
        val defaultLanguage = "dummy" // TODO: eradicate hack
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
        holder.update(position, names[position])
    }

    override fun getItemCount() = names.size

    private fun remove(index: Int) {
        if (index < 1) return
        names.removeAt(index)
        notifyItemRemoved(index)
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
        }

        fun update(index: Int, ln: Name) {
            Name = ln

            val isFirst = index == 0


            autoCorrectInput.setText(Name.name)
            autoCorrectInput.requestFocus()

            autoCorrectInput.setTypeface(null, if (isFirst) Typeface.BOLD else Typeface.NORMAL)

            updateNameSuggestions()
        }

        private fun updateNameSuggestions() {
            val NameSuggestionsMap = getNameSuggestionsByLanguageCode(Name.languageCode)

            val nameInputNotEmpty = autoCorrectInput.text.toString().trim().isNotEmpty()
            val hasNoNameSuggestions = NameSuggestionsMap.isEmpty()
            buttonNameSuggestions.visibility =
                    if (hasNoNameSuggestions || nameInputNotEmpty) View.GONE else View.VISIBLE

            buttonNameSuggestions.setOnClickListener { v ->
                showNameSuggestionsMenu(v, NameSuggestionsMap) { selection ->
                    names = selection.toNameList()
                    notifyDataSetChanged()
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
