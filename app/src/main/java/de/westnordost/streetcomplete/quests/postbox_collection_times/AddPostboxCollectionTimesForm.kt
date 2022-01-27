package de.westnordost.streetcomplete.quests.postbox_collection_times

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestCollectionTimesBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.osm.opening_hours.parser.toCollectionTimesRows
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHoursRules
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class AddPostboxCollectionTimesForm : AbstractQuestFormAnswerFragment<CollectionTimesAnswer>() {

    override val contentLayoutResId = R.layout.quest_collection_times
    private val binding by contentViewBinding(QuestCollectionTimesBinding::bind)

    override val buttonPanelAnswers get() =
        if(isDisplayingPreviousCollectionTimes) listOf(
            AnswerItem(R.string.quest_generic_hasFeature_no) { setAsResurvey(false) },
            AnswerItem(R.string.quest_generic_hasFeature_yes) {
                applyAnswer(CollectionTimes(osmElement!!.tags["collection_times"]!!.toOpeningHoursRules()!!))
            }
        )
        else emptyList()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_collectionTimes_answer_no_times_specified) { confirmNoTimes() }
    )

    private lateinit var collectionTimesAdapter: CollectionTimesAdapter

    private var isDisplayingPreviousCollectionTimes: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        collectionTimesAdapter = CollectionTimesAdapter(requireContext(), countryInfo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        } else {
            initStateFromTags()
        }

        collectionTimesAdapter.registerAdapterDataObserver( AdapterDataChangedWatcher { checkIsFormComplete() })

        binding.collectionTimesList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.collectionTimesList.adapter = collectionTimesAdapter
        binding.collectionTimesList.isNestedScrollingEnabled = false
        checkIsFormComplete()

        binding.addTimesButton.setOnClickListener { onClickAddButton(it) }
    }

    private fun onClickAddButton(v: View) {
        val rows = collectionTimesAdapter.collectionTimesRows

        val addTimeAvailable = rows.isNotEmpty()

        if (addTimeAvailable) {
            val popup = PopupMenu(requireContext(), v)
            if (addTimeAvailable) popup.menu.add(Menu.NONE, 0, Menu.NONE, R.string.quest_openingHours_add_hours)
            popup.menu.add(Menu.NONE, 1, Menu.NONE, R.string.quest_openingHours_add_weekdays)
            popup.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    0 -> collectionTimesAdapter.addNewHours()
                    1 -> collectionTimesAdapter.addNewWeekdays()
                }
                true
            }
            popup.show()
        } else {
            collectionTimesAdapter.addNewWeekdays()
        }
    }

    private fun initStateFromTags() {
        val ct = osmElement!!.tags["collection_times"]
        val rows = ct?.toOpeningHoursRules()?.toCollectionTimesRows()
        if (rows != null) {
            collectionTimesAdapter.collectionTimesRows = rows.toMutableList()
            setAsResurvey(true)
        } else {
            setAsResurvey(false)
        }
    }

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        collectionTimesAdapter.collectionTimesRows = Json.decodeFromString(savedInstanceState.getString(TIMES_DATA)!!)
        setAsResurvey(savedInstanceState.getBoolean(IS_DISPLAYING_PREVIOUS_TIMES))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TIMES_DATA, Json.encodeToString(collectionTimesAdapter.collectionTimesRows))
    }

    override fun onClickOk() {
        applyAnswer(CollectionTimes(collectionTimesAdapter.createCollectionTimes()))
    }

    private fun setAsResurvey(resurvey: Boolean) {
        collectionTimesAdapter.isEnabled = !resurvey
        isDisplayingPreviousCollectionTimes = resurvey
        binding.addTimesButton.isGone = resurvey
        updateButtonPanel()
    }

    private fun confirmNoTimes() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoCollectionTimesSign) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = collectionTimesAdapter.collectionTimesRows.isNotEmpty() && !isDisplayingPreviousCollectionTimes

    companion object {
        private const val TIMES_DATA = "times_data"
        private const val IS_DISPLAYING_PREVIOUS_TIMES = "ct_is_displaying_previous_times"
    }
}
