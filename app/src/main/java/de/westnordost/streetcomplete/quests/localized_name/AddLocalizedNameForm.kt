package de.westnordost.streetcomplete.quests.localized_name

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView

import java.util.ArrayList
import java.util.Queue

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.streetcomplete.ktx.toObject
import kotlinx.android.synthetic.main.quest_localizedname.*


abstract class AddLocalizedNameForm : AbstractQuestFormAnswerFragment() {

    override val contentLayoutResId = R.layout.quest_localizedname

    @Inject internal lateinit var serializer: Serializer

    protected lateinit var adapter: AddLocalizedNameAdapter

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLocalizedNameAdapter(savedInstanceState)
    }

    private fun initLocalizedNameAdapter(savedInstanceState: Bundle?) {
        val data: ArrayList<LocalizedName> = if (savedInstanceState != null) {
            serializer.toObject(savedInstanceState.getByteArray(LOCALIZED_NAMES_DATA)!!)
        } else {
            ArrayList()
        }

        adapter = setupNameAdapter(data, addLanguageButton)
        adapter.addOnNameChangedListener { checkIsFormComplete() }
        adapter.registerAdapterDataObserver(AdapterDataChangedWatcher { checkIsFormComplete() })
        namesList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        namesList.adapter = adapter
        namesList.isNestedScrollingEnabled = false
        checkIsFormComplete()
    }

    protected open fun setupNameAdapter(data: List<LocalizedName>, addLanguageButton: Button) =
        AddLocalizedNameAdapter(data, activity!!, getPossibleStreetsignLanguages(), null, null, addLanguageButton)

    protected fun getPossibleStreetsignLanguages(): List<String> {
        val result = mutableListOf<String>()
        result.addAll(countryInfo.officialLanguages)
        result.addAll(countryInfo.additionalStreetsignLanguages)
        return result.distinct()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val serializedNames = serializer.toBytes(ArrayList(adapter.localizedNames))
        outState.putByteArray(LOCALIZED_NAMES_DATA, serializedNames)
    }

    protected open fun applyNameAnswer() {
        applyAnswer(createAnswer())
    }

    protected fun createAnswer(): Bundle {
        val data = adapter.localizedNames

        val names = arrayOfNulls<String>(data.size)
        val languageCodes = arrayOfNulls<String>(data.size)
        for (i in data.indices) {
            names[i] = data[i].name
            languageCodes[i] = data[i].languageCode
        }

        return bundleOf(
            NAMES to names,
            LANGUAGE_CODES to languageCodes
        )
    }

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

    protected fun confirmPossibleAbbreviation(name: String, onConfirmed: () -> Unit) {
        val title = Html.fromHtml(
            resources.getString(
                R.string.quest_streetName_nameWithAbbreviations_confirmation_title_name,
                "<i>" + Html.escapeHtml(name) + "</i>"
            )
        )

        AlertDialog.Builder(activity!!)
            .setTitle(title)
            .setMessage(R.string.quest_streetName_nameWithAbbreviations_confirmation_description)
            .setPositiveButton(R.string.quest_streetName_nameWithAbbreviations_confirmation_positive) { _, _ -> onConfirmed() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    protected fun showKeyboardInfo() {
        AlertDialog.Builder(activity!!)
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

    // all added name rows are not empty
    override fun isFormComplete() = adapter.localizedNames.all { it.name.trim().isNotEmpty() }

    companion object {
        protected val LOCALIZED_NAMES_DATA = "localized_names_data"

        const val NO_NAME = "no_name"
        const val NAMES = "names"
        const val LANGUAGE_CODES = "language_codes"
    }
}

internal fun Bundle.toNameByLanguage(): Map<String, String> {
    val names = getStringArray(AddLocalizedNameForm.NAMES)!!
    val languages = getStringArray(AddLocalizedNameForm.LANGUAGE_CODES)!!

    val result = mutableMapOf<String, String>()
    result[""] = names[0]
    // add languages only if there is more than one name specified. If there is more than one
    // name, the "main" name (name specified in top row) is also added with the language.
    if (names.size > 1) {
        for (i in names.indices) {
            // (the first) element may have no specific language
            if (!languages[i].isEmpty()) {
                result[languages[i]] = names[i]
            }
        }
    }
    return result
}
