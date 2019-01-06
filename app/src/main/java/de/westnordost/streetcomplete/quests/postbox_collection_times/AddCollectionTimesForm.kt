package de.westnordost.streetcomplete.quests.postbox_collection_times

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View

import java.util.ArrayList

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.streetcomplete.ktx.toObject
import kotlinx.android.synthetic.main.quest_collection_times.*


class AddCollectionTimesForm : AbstractQuestFormAnswerFragment() {

    override val contentLayoutResId = R.layout.quest_collection_times

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_collectionTimes_answer_no_times_specified) { confirmNoTimes() }
    )

    private lateinit var collectionTimesAdapter: CollectionTimesAdapter

    @Inject internal lateinit var serializer: Serializer

    private val collectionTimesString get() =
        collectionTimesAdapter.createCollectionTimes().joinToString(", ")

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewData = loadCollectionTimesData(savedInstanceState)
        collectionTimesAdapter = CollectionTimesAdapter(viewData, context!!, countryInfo)
        collectionTimesAdapter.registerAdapterDataObserver( AdapterDataChangedWatcher { checkIsFormComplete() })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectionTimesList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        collectionTimesList.adapter = collectionTimesAdapter
        collectionTimesList.isNestedScrollingEnabled = false
        checkIsFormComplete()

        addTimesButton.setOnClickListener { collectionTimesAdapter.addNew() }
    }

    private fun loadCollectionTimesData(savedInstanceState: Bundle?):List<WeekdaysTimesRow> =
        if (savedInstanceState != null) {
            serializer.toObject<ArrayList<WeekdaysTimesRow>>(savedInstanceState.getByteArray(TIMES_DATA))
        } else {
            listOf()
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val serializedTimes = serializer.toBytes(ArrayList(collectionTimesAdapter.collectionTimesRows))
        outState.putByteArray(TIMES_DATA, serializedTimes)
    }

    override fun onClickOk() {
        val answer = Bundle()
        answer.putString(TIMES, collectionTimesString)
        applyAnswer(answer)
    }

    private fun confirmNoTimes() {
        AlertDialog.Builder(context!!)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyNoTimesAnswer() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    private fun applyNoTimesAnswer() {
        val answer = Bundle()
        answer.putBoolean(NO_TIMES_SPECIFIED, true)
        applyAnswer(answer)
    }

    override fun isFormComplete() = collectionTimesString.isNotEmpty()

    companion object {
        const val TIMES = "times"
        const val NO_TIMES_SPECIFIED = "no_times_specified"

        private const val TIMES_DATA = "times_data"
    }
}
