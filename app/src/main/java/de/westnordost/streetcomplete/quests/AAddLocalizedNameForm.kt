package de.westnordost.streetcomplete.quests

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.text.parseAsHtml
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.view.AdapterDataChangedWatcher
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import java.util.Queue

abstract class AAddLocalizedNameForm<T> : AbstractOsmQuestForm<T>() {

    protected abstract val addLanguageButton: View
    protected abstract val namesList: RecyclerView

    private val prefs: Preferences by inject()

    open val adapterRowLayoutResId = R.layout.row_localizedname

    protected var adapter: LocalizedNameAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLocalizedNameAdapter(
            savedInstanceState?.getString(LOCALIZED_NAMES_DATA)?.let { Json.decodeFromString(it) }
        )
    }

    private fun initLocalizedNameAdapter(data: MutableList<LocalizedName>? = null) {
        val selectableLanguages = getSelectableLanguageTags().toMutableList()
        val preferredLanguage = prefs.preferredLanguageForNames
        if (preferredLanguage != null) {
            if (selectableLanguages.remove(preferredLanguage)) {
                selectableLanguages.add(0, preferredLanguage)
            }
        }

        val adapter = LocalizedNameAdapter(
            data.orEmpty(),
            requireContext(),
            selectableLanguages,
            getAbbreviationsByLocale(),
            getLocalizedNameSuggestions(),
            addLanguageButton,
            adapterRowLayoutResId
        )
        adapter.addOnNameChangedListener { checkIsFormComplete() }
        adapter.registerAdapterDataObserver(AdapterDataChangedWatcher { checkIsFormComplete() })
        lifecycle.addObserver(adapter)
        this.adapter = adapter
        namesList.adapter = adapter
        namesList.isNestedScrollingEnabled = false
        checkIsFormComplete()
    }

    protected open fun getSelectableLanguageTags(): List<String> =
        (countryInfo.officialLanguages + countryInfo.additionalStreetsignLanguages).distinct()

    protected open fun getAbbreviationsByLocale(): AbbreviationsByLocale? = null

    protected open fun getLocalizedNameSuggestions(): List<List<LocalizedName>>? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter?.names?.let { outState.putString(LOCALIZED_NAMES_DATA, Json.encodeToString(it)) }
    }

    final override fun onClickOk() {
        onClickOk(adapter?.names.orEmpty())

        val firstLanguage = adapter?.names?.firstOrNull()?.languageTag?.takeIf { it.isNotBlank() }
        if (firstLanguage != null) prefs.preferredLanguageForNames = firstLanguage
    }

    abstract fun onClickOk(names: List<LocalizedName>)

    protected fun confirmPossibleAbbreviationsIfAny(names: Queue<String>, onConfirmedAll: () -> Unit) {
        if (names.isEmpty()) {
            onConfirmedAll()
        } else {
            /* recursively call self on confirm until the list of not-abbreviations to confirm is
               through */
            val name = names.remove()
            confirmPossibleAbbreviation(name) { confirmPossibleAbbreviationsIfAny(names, onConfirmedAll) }
        }
    }

    private fun confirmPossibleAbbreviation(name: String, onConfirmed: () -> Unit) {
        val title = resources.getString(
            R.string.quest_streetName_nameWithAbbreviations_confirmation_title_name,
            "<i>" + Html.escapeHtml(name) + "</i>"
        ).parseAsHtml()

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(R.string.quest_streetName_nameWithAbbreviations_confirmation_description)
            .setPositiveButton(R.string.quest_streetName_nameWithAbbreviations_confirmation_positive) { _, _ -> onConfirmed() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    protected fun showKeyboardInfo() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_streetName_cantType_title)
            .setMessage(R.string.quest_streetName_cantType_description)
            .setPositiveButton(R.string.quest_streetName_cantType_open_settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
            .setNeutralButton(R.string.quest_streetName_cantType_open_store) { _, _ ->
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_MARKET)
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun isFormComplete(): Boolean =
        adapter?.names.orEmpty().isNotEmpty()

    companion object {
        private const val LOCALIZED_NAMES_DATA = "localized_names_data"
    }
}
