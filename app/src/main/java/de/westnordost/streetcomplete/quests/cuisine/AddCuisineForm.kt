package de.westnordost.streetcomplete.quests.cuisine

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestCuisineSuggestionBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.mostCommonWithin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddCuisineForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.quest_cuisine_suggestion
    private val binding by contentViewBinding(QuestCuisineSuggestionBinding::bind)

    val cuisines = mutableSetOf<String>()

    val cuisine get() = binding.cuisineInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (suggestions.isEmpty()) { // load suggestions
            requireContext().assets.open("cuisine/cuisineSuggestions.txt").bufferedReader()
                .lineSequence().forEach { if (it.isNotBlank()) suggestions.add(it.trim().intern()) }
        }

        binding.cuisineInput.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                (lastPickedAnswers + suggestions).distinct()
            )
        )
        binding.cuisineInput.onItemClickListener = AdapterView.OnItemClickListener { _, t, _, _ ->
            val cuisine = (t as? TextView)?.text?.toString() ?: return@OnItemClickListener
            if (!cuisines.add(cuisine)) return@OnItemClickListener // we don't want duplicates
            onAddedCuisine(cuisine)
        }

        binding.cuisineInput.doAfterTextChanged { checkIsFormComplete() }
        binding.cuisineInput.doOnLayout { binding.cuisineInput.dropDownWidth = binding.cuisineInput.width - requireContext().dpToPx(60).toInt() }

        binding.addCuisineButton.setOnClickListener {
            if (!isFormComplete() || binding.cuisineInput.text.isBlank()) return@setOnClickListener
            cuisines.add(cuisine)
            onAddedCuisine(cuisine)
        }
        viewLifecycleScope.launch {
            delay(20) // delay, because otherwise dropdown is not anchored at the correct view
            binding.cuisineInput.showDropDown()
        }
    }

    override fun onClickOk() {
        cuisines.removeAll { it.isBlank() }
        if (cuisines.isNotEmpty()) favs.add(cuisines)
        if (cuisine.isNotBlank()) favs.add(cuisine)
        if (cuisine.isBlank())
            applyAnswer(cuisines.joinToString(";"))
        else
            applyAnswer((cuisines + listOf(cuisine)).joinToString(";"))
    }

    override fun isFormComplete() = (cuisine.isNotBlank() || cuisines.isNotEmpty()) && !cuisine.contains(";") && !cuisines.contains(cuisine)

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext),
            key = javaClass.simpleName,
            serialize = { it },
            deserialize = { it },
        )
    }

    private fun onAddedCuisine(cuisine: String) {
        binding.currentCuisines.text = cuisines.joinToString(";")
        binding.cuisineInput.text.clear()
        (binding.cuisineInput.adapter as ArrayAdapter<String>).remove(cuisine)
        viewLifecycleScope.launch {
            delay(30) // delay, because otherwise dropdown disappears immediately (also the remove apparently is done in background, so it needs some time)
            binding.cuisineInput.showDropDown()
        }
    }

    private lateinit var favs: LastPickedValuesStore<String>

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 20, historyCount = 50, first = 1)
            .toList()
    }

    companion object {
        private val suggestions = mutableListOf<String>()
    }
}
