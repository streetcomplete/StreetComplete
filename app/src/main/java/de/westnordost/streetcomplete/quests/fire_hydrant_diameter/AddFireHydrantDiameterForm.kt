package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestFireHydrantDiameterBinding
import de.westnordost.streetcomplete.databinding.QuestFireHydrantDiameterLastPickedButtonBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameterMeasurementUnit.INCH
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameterMeasurementUnit.MILLIMETER
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.ktx.intOrNull
import de.westnordost.streetcomplete.util.mostCommonWithin

class AddFireHydrantDiameterForm : AbstractOsmQuestForm<FireHydrantDiameterAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    override val contentLayoutResId = R.layout.quest_fire_hydrant_diameter
    private val binding by contentViewBinding(QuestFireHydrantDiameterBinding::bind)

    private val diameterValue get() = binding.diameterInput.intOrNull ?: 0

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 5, historyCount = 15, first = 1)
            .sorted()
            .toList()
    }

    private lateinit var favs: LastPickedValuesStore<Int>

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext),
            key = javaClass.simpleName,
            serialize = { it.toString() },
            deserialize = { it.toInt() }
        )
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.diameterInput.doAfterTextChanged { checkIsFormComplete() }

        binding.lastPickedButtons.adapter = LastPickedAdapter(lastPickedAnswers, ::onLastPickedButtonClicked)
    }

    private fun onLastPickedButtonClicked(position: Int) {
        binding.diameterInput.setText(lastPickedAnswers[position].toString())
    }

    override fun isFormComplete() = diameterValue > 0

    override fun onClickOk() {
        val diameter = if (countryInfo.countryCode == "GB" && diameterValue <= 25) {
            FireHydrantDiameter(diameterValue, INCH)
        } else {
            FireHydrantDiameter(diameterValue, MILLIMETER)
        }

        if (isUnusualDiameter(diameter)) {
            confirmUnusualInput(diameter.unit) {
                favs.add(diameterValue)
                applyAnswer(diameter)
            }
        } else {
            favs.add(diameterValue)
            applyAnswer(diameter)
        }
    }

    private fun isUnusualDiameter(diameter: FireHydrantDiameter): Boolean {
        val value = diameter.value
        return when (diameter.unit) {
            MILLIMETER -> value > 600 || value < 50 || value % 5 != 0
            INCH -> value < 1 || value > 25
        }
    }

    private fun confirmUnusualInput(
        unit: FireHydrantDiameterMeasurementUnit,
        onConfirmed: () -> Unit
    ) {
        val min = if (unit == MILLIMETER) 80 else 3
        val max = if (unit == MILLIMETER) 300 else 12
        val msg = getString(
            R.string.quest_fireHydrant_diameter_unusualInput_confirmation_description2,
            min, max
        )
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(msg)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> onConfirmed() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }

    private fun confirmNoSign() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_fireHydrant_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoFireHydrantDiameterSign) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}

private class LastPickedAdapter(
    private val lastPickedAnswers: List<Int>,
    private val onItemClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<LastPickedAdapter.ViewHolder>() {

    class ViewHolder(
        private val binding: QuestFireHydrantDiameterLastPickedButtonBinding,
        private val onItemClicked: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { onItemClicked(bindingAdapterPosition) }
        }

        fun onBind(item: Int) {
            binding.lastDiameterLabel.text = item.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = QuestFireHydrantDiameterLastPickedButtonBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.onBind(lastPickedAnswers[position])
    }

    override fun getItemCount() = lastPickedAnswers.size
}
