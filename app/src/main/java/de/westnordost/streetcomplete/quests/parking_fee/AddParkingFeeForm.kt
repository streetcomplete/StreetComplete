package de.westnordost.streetcomplete.quests.parking_fee

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestFeeHoursBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.opening_hours.adapter.RegularOpeningHoursAdapter
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningHoursRow
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AddParkingFeeForm : AbstractQuestFormAnswerFragment<FeeAnswer>() {

    override val contentLayoutResId = R.layout.quest_fee_hours
    private val binding by contentViewBinding(QuestFeeHoursBinding::bind)

    override val buttonPanelAnswers get() =
        if(!isDefiningHours) listOf(
            AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(HasNoFee) },
            AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(HasFee) }
        )
        else emptyList()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_fee_answer_hours) { isDefiningHours = true }
    )

    private lateinit var openingHoursAdapter: RegularOpeningHoursAdapter

    private var content: ViewGroup? = null

    private var isDefiningHours: Boolean = false
    set(value) {
        field = value

        content?.isGone = !value
        updateButtonPanel()
    }
    private var isFeeOnlyAtHours: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        openingHoursAdapter = RegularOpeningHoursAdapter(requireContext(), countryInfo)
        openingHoursAdapter.rows = loadOpeningHoursData(savedInstanceState).toMutableList()
        openingHoursAdapter.registerAdapterDataObserver( AdapterDataChangedWatcher { checkIsFormComplete() })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        content = view.findViewById(R.id.content)

        // must be read here because setting these values effects the UI
        isFeeOnlyAtHours = savedInstanceState?.getBoolean(IS_FEE_ONLY_AT_HOURS, true) ?: true
        isDefiningHours = savedInstanceState?.getBoolean(IS_DEFINING_HOURS) ?: false

        binding.openingHoursList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.openingHoursList.adapter = openingHoursAdapter
        binding.openingHoursList.isNestedScrollingEnabled = false
        checkIsFormComplete()

        binding.addTimesButton.setOnClickListener { openingHoursAdapter.addNewWeekdays() }

        val spinnerItems = listOf(
            getString(R.string.quest_fee_only_at_hours),
            getString(R.string.quest_fee_not_at_hours)
        )
        binding.selectFeeOnlyAtHours.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_centered, spinnerItems)
        binding.selectFeeOnlyAtHours.setSelection(if (isFeeOnlyAtHours) 0 else 1)
        binding.selectFeeOnlyAtHours.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                isFeeOnlyAtHours = position == 0
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        content = null
    }

    override fun onClickOk() {
        val times = openingHoursAdapter.createOpeningHours()
        applyAnswer(if(isFeeOnlyAtHours) HasFeeAtHours(times) else HasFeeExceptAtHours(times))
    }

    private fun loadOpeningHoursData(savedInstanceState: Bundle?): List<OpeningHoursRow> =
        savedInstanceState?.let { Json.decodeFromString(it.getString(OPENING_HOURS_DATA)!!) } ?: emptyList()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(OPENING_HOURS_DATA, Json.encodeToString(openingHoursAdapter.rows))
        outState.putBoolean(IS_DEFINING_HOURS, isDefiningHours)
        outState.putBoolean(IS_FEE_ONLY_AT_HOURS, isFeeOnlyAtHours)
    }

    override fun isRejectingClose() =
        isDefiningHours && openingHoursAdapter.rows.isEmpty()

    override fun isFormComplete() =
        isDefiningHours && openingHoursAdapter.rows.isNotEmpty()

    companion object {
        private const val OPENING_HOURS_DATA = "oh_data"
        private const val IS_FEE_ONLY_AT_HOURS = "oh_fee_only_at"
        private const val IS_DEFINING_HOURS = "oh"
    }
}
