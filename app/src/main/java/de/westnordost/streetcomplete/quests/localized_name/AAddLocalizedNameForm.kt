package de.westnordost.streetcomplete.quests.localized_name

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Html
import android.view.View
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

abstract class AAddLocalizedNameForm<T> : AbstractQuestFormAnswerFragment<T>() {

    override val contentLayoutResId = R.layout.quest_localizedname

    private val serializer: Serializer

    protected lateinit var adapter: AddLocalizedNameAdapter

    init {
        val fields = InjectedFields()
        Injector.instance.applicationComponent.inject(fields)
        serializer = fields.serializer
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

    protected open fun setupNameAdapter(data: List<LocalizedName>, addLanguageButton: View) =
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

    final override fun onClickOk() {
        onClickOk(createOsmModel())
    }

    private fun createOsmModel(): List<LocalizedName> {
        val data = adapter.localizedNames.toMutableList()
        // language is only specified explicitly in OSM (usually) if there is more than one name specified
        if(data.size == 1) {
            data[0].languageCode = ""
        }
        // but if there is more than one language, ensure that a "main" name is also specified
        else {
            val mainLanguageIsSpecified = data.indexOfFirst { it.languageCode == "" } >= 0
            // use the name specified in the top row for that
            if(!mainLanguageIsSpecified) {
                data.add(LocalizedName("", data[0].name))
            }
        }
        return data
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
    override fun isFormComplete() = adapter.localizedNames.isNotEmpty()
            && adapter.localizedNames.all { it.name.trim().isNotEmpty() }


    internal class InjectedFields {
        @Inject internal lateinit var serializer: Serializer
    }

    companion object {
        private const val LOCALIZED_NAMES_DATA = "localized_names_data"
    }
}
