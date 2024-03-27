package de.westnordost.streetcomplete.quests.building_levels

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestBuildingLevelsBinding
import de.westnordost.streetcomplete.databinding.QuestBuildingLevelsLastPickedButtonBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.ktx.intOrNull
import de.westnordost.streetcomplete.util.mostCommonWithin
import org.koin.android.ext.android.inject

class AddBuildingLevelsForm : AbstractOsmQuestForm<BuildingLevelsAnswer>() {

    override val contentLayoutResId = R.layout.quest_building_levels
    private val binding by contentViewBinding(QuestBuildingLevelsBinding::bind)

    private val prefs: ObservableSettings by inject()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_buildingLevels_answer_multipleLevels) { showMultipleLevelsHint() }
    )

    private val levels get() = binding.levelsInput.intOrNull?.takeIf { it >= 0 }
    private val roofLevels get() = binding.roofLevelsInput.intOrNull?.takeIf { it >= 0 }

    private lateinit var favs: LastPickedValuesStore<BuildingLevelsAnswer>

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 5, historyCount = 15, first = 1)
            .sortedWith(compareBy<BuildingLevelsAnswer> { it.levels }.thenBy { it.roofLevels })
            .toList()
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            prefs,
            key = javaClass.simpleName,
            serialize = { listOfNotNull(it.levels, it.roofLevels).joinToString("#") },
            deserialize = { value ->
                value.split("#").let { BuildingLevelsAnswer(it[0].toInt(), it.getOrNull(1)?.toInt()) }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            binding.levelsInput.setText(element.tags["building:levels"])
            binding.roofLevelsInput.setText(element.tags["roof:levels"])
        }
        val focusedInput = if (levels == null) binding.levelsInput else binding.roofLevelsInput
        focusedInput.requestFocus()
        focusedInput.selectAll()

        binding.levelsInput.doAfterTextChanged { checkIsFormComplete() }
        binding.roofLevelsInput.doAfterTextChanged { checkIsFormComplete() }

        binding.lastPickedButtons.adapter = LastPickedAdapter(lastPickedAnswers, ::onLastPickedButtonClicked)
    }

    private fun onLastPickedButtonClicked(position: Int) {
        val buildingLevelsAnswer = lastPickedAnswers[position]
        binding.levelsInput.setText(buildingLevelsAnswer.levels.toString())
        binding.roofLevelsInput.setText(buildingLevelsAnswer.roofLevels?.toString() ?: "")
    }

    override fun onClickOk() {
        val answer = BuildingLevelsAnswer(levels!!, roofLevels)
        favs.add(answer)
        applyAnswer(answer)
    }

    private fun showMultipleLevelsHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_buildingLevels_answer_description)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    override fun isFormComplete(): Boolean {
        val hasNonFlatRoofShape = element.tags.containsKey("roof:shape") && element.tags["roof:shape"] != "flat"
        val roofLevelsAreOptional = countryInfo.roofsAreUsuallyFlat && !hasNonFlatRoofShape
        return levels != null && (roofLevelsAreOptional || roofLevels != null)
    }
}

private class LastPickedAdapter(
    private val lastPickedAnswers: List<BuildingLevelsAnswer>,
    private val onItemClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<LastPickedAdapter.ViewHolder>() {

    class ViewHolder(
        private val binding: QuestBuildingLevelsLastPickedButtonBinding,
        private val onItemClicked: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { onItemClicked(bindingAdapterPosition) }
        }

        fun onBind(item: BuildingLevelsAnswer) {
            binding.lastLevelsLabel.text = item.levels.toString()
            binding.lastRoofLevelsLabel.text = item.roofLevels?.toString() ?: " "
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = QuestBuildingLevelsLastPickedButtonBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.onBind(lastPickedAnswers[position])
    }

    override fun getItemCount() = lastPickedAnswers.size
}
