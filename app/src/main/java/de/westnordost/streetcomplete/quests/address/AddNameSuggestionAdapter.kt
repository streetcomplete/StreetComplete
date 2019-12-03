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

data class Name(var languageCode: String, var name: String)

class AddNameSuggestionAdapter(
        initialNames: List<Name>,
        private val context: Context,
        private val NameSuggestions: List<MutableMap<String, String>>?
) : RecyclerView.Adapter<AddNameSuggestionAdapter.ViewHolder>() {

    var name: String
        private set
    private val listeners = mutableListOf<(Name) -> Unit>()

    init {
        name = initialNames.toMutableList().firstOrNull()?.name ?: ""
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
        holder.update(position, Name("", name))
    }

    override fun getItemCount() = 1 // what if name is null TODO

    /** Show a context menu above the given [view] where the user can select one key from the
     * [NameSuggestionsMap]. The value of the selected key will be passed to the
     * [callback] */
    private fun showNameSuggestionsMenu(
            view: View,
            NameSuggestionList: List<String>,
            callback: (String) -> Unit
    ) {
        val popup = PopupMenu(context, view)

        for ((i, key) in NameSuggestionList.withIndex()) {
            popup.menu.add(NONE, i, NONE, key)
        }

        popup.setOnMenuItemClickListener { item ->
            callback(item.title.toString())
            true
        }
        popup.show()
    }

    private fun getNameSuggestionsByLanguageCode(languageCode: String): List<String> {
        val nameSuggestionsList = mutableListOf<String>()
        if (NameSuggestions != null) {
            for (NameSuggestion in NameSuggestions) {
                val name = NameSuggestion[languageCode] ?: continue
                nameSuggestionsList += name
            }
        }
        return nameSuggestionsList
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
            autoCorrectInput.setText(Name.name)
            autoCorrectInput.requestFocus()
            autoCorrectInput.setTypeface(null, Typeface.BOLD)
            updateNameSuggestions()
        }

        private fun updateNameSuggestions() {
            val nameSuggestionList = getNameSuggestionsByLanguageCode(Name.languageCode)

            val nameInputNotEmpty = autoCorrectInput.text.toString().trim().isNotEmpty()
            val hasNoNameSuggestions = nameSuggestionList.isEmpty()
            buttonNameSuggestions.visibility =
                    if (hasNoNameSuggestions || nameInputNotEmpty) View.GONE else View.VISIBLE

            buttonNameSuggestions.setOnClickListener { v ->
                showNameSuggestionsMenu(v, nameSuggestionList) { selected ->
                    name = selected
                    notifyDataSetChanged()
                }
            }
        }
    }
}
