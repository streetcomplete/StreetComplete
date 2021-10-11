package de.westnordost.streetcomplete.quests.building_levels

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestBuildingLevelsBinding
import de.westnordost.streetcomplete.databinding.QuestBuildingLevelsLastPickedButtonBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.LastPickedValuesStore
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.TextChangedWatcher

class AddBuildingLevelsForm : AbstractQuestFormAnswerFragment<BuildingLevelsAnswer>() {

    override val contentLayoutResId = R.layout.quest_building_levels
    private val binding by contentViewBinding(QuestBuildingLevelsBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_buildingLevels_answer_multipleLevels) { showMultipleLevelsHint() }
    )

    private val levels get() = binding.levelsInput.text?.toString().orEmpty().trim()
    private val roofLevels get() = binding.roofLevelsInput.text?.toString().orEmpty().trim()

    private val lastPickedAnswers by lazy {
        favs.get(javaClass.simpleName).map { it.toBuildingLevelAnswer() }.sortedWith(
            compareBy<BuildingLevelsAnswer> { it.levels }.thenBy { it.roofLevels }
        )
    }

    @Inject internal lateinit var favs: LastPickedValuesStore<String>

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onTextChangedListener = TextChangedWatcher {
            checkIsFormComplete()
        }

        binding.levelsInput.requestFocus()
        binding.levelsInput.addTextChangedListener(onTextChangedListener)
        binding.roofLevelsInput.addTextChangedListener(onTextChangedListener)

        binding.lastPickedButtons.adapter = LastPickedAdapter(lastPickedAnswers, ::onLastPickedButtonClicked)
    }

    private fun onLastPickedButtonClicked(position: Int) {
        binding.levelsInput.setText(lastPickedAnswers[position].levels.toString())
        binding.roofLevelsInput.setText(lastPickedAnswers[position].roofLevels?.toString() ?: "")
    }

    override fun onClickOk() {
        val roofLevelsNumber = if (roofLevels.isEmpty()) null else roofLevels.toInt()
        val answer = BuildingLevelsAnswer(levels.toInt(), roofLevelsNumber)
        favs.add(javaClass.simpleName, answer.toSerializedString(), max = 5)
        applyAnswer(answer)
    }

    private fun showMultipleLevelsHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_buildingLevels_answer_description)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    override fun isFormComplete() = levels.isNotEmpty()
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

private fun BuildingLevelsAnswer.toSerializedString() =
    listOfNotNull(levels, roofLevels).joinToString("#")

private fun String.toBuildingLevelAnswer() =
    this.split("#").let { BuildingLevelsAnswer(it[0].toInt(), it.getOrNull(1)?.toInt()) }
