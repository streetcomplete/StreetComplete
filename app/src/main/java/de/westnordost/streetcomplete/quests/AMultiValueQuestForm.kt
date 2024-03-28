package de.westnordost.streetcomplete.quests

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestMultiValueBinding
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.mostCommonWithin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** form for adding multiple values to a single key */
abstract class AMultiValueQuestForm<T> : AbstractOsmQuestForm<T>() {

    override val contentLayoutResId = R.layout.quest_multi_value
    private val binding by contentViewBinding(QuestMultiValueBinding::bind)

    /** convert the multi-value string answer to type T */
    abstract fun stringToAnswer(answerString: String): T

    /**
     *  provide suggestions, loaded once and stored in companion object
     *  shown below all other suggestions
    */
    abstract fun getConstantSuggestions(): Collection<String>

    /**
     *  provide suggestions, loaded every time the form is opened
     *  shown above all other suggestions
     */
    open fun getVariableSuggestions(): Collection<String> = emptyList()

    /** text for the addValueButton */
    abstract val addAnotherValueResId: Int

    open val onlyAllowSuggestions = false

    private val values = mutableSetOf<String>()

    private val value get() = binding.valueInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addValueButton.setText(addAnotherValueResId)

        binding.valueInput.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                (getVariableSuggestions() + lastPickedAnswers + getSuggestions()).distinct()
            )
        )
        binding.valueInput.onItemClickListener = AdapterView.OnItemClickListener { _, t, _, _ ->
            val value = (t as? TextView)?.text?.toString() ?: return@OnItemClickListener
            addValue(value)
        }

        binding.valueInput.doAfterTextChanged {
            if (it.toString().endsWith("\n"))
                addValue(it.toString())
            checkIsFormComplete()
        }
        binding.valueInput.doOnLayout { binding.valueInput.dropDownWidth = binding.valueInput.width - requireContext().resources.dpToPx(60).toInt() }

        binding.addValueButton.setOnClickListener {
            if (!isFormComplete() || binding.valueInput.text.isBlank()) return@setOnClickListener
            addValue(value)
        }
        showSuggestions()
    }

    override fun onClickOk() {
        values.removeAll { it.isBlank() }
        if (values.isNotEmpty()) favs.add(values)
        if (value.isNotBlank()) favs.add(value)
        if (value.isBlank())
            applyAnswer(stringToAnswer(values.joinToString(";")))
        else
            applyAnswer(stringToAnswer((values + listOf(value)).joinToString(";")))
    }

    override fun isFormComplete() = (value.isNotBlank() || values.isNotEmpty()) && !value.contains(";")
        && !values.contains(value)
        && (!onlyAllowSuggestions || (values.all { getSuggestions().contains(it) } && (getSuggestions().contains(value) || value.isBlank())))

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            prefs,
            key = javaClass.simpleName,
            serialize = { it },
            deserialize = { it },
        )
    }

    private fun addValue(value: String) {
        val modifiedValue = value.trim()
        if (modifiedValue.isEmpty()) return
        if (!values.add(modifiedValue)) return // we don't want duplicates
        onAddedValue(modifiedValue)
    }

    private fun onAddedValue(value: String) {
        binding.currentValues.text = values.joinToString(";")
        binding.valueInput.text.clear()
        (binding.valueInput.adapter as ArrayAdapter<String>).remove(value)
        showSuggestions()
    }

    private lateinit var favs: LastPickedValuesStore<String>

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 20, historyCount = 50, first = 1)
            .toList()
    }

    private fun showSuggestions() {
        viewLifecycleScope.launch {
            delay(30) // delay, because otherwise it sometimes doesn't work properly
            binding.valueInput.showDropDown()
        }
    }

    private fun getSuggestions(): Collection<String> =
        suggestions.getOrPut(this::class.simpleName!!) { getConstantSuggestions() }

    companion object {
        private val suggestions = hashMapOf<String, Collection<String>>()
    }
}
