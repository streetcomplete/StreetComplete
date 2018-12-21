package de.westnordost.streetcomplete.quests.postbox_collection_times

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.util.Serializer
import kotlinx.android.synthetic.main.quest_collection_times.*


class AddCollectionTimesForm : AbstractQuestFormAnswerFragment() {

    private lateinit var collectionTimesAdapter: CollectionTimesAdapter

    @Inject internal lateinit var serializer: Serializer

    private val collectionTimesString get() =
        collectionTimesAdapter.createCollectionTimes().joinToString(", ")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        Injector.instance.applicationComponent.inject(this)

        setContentView(R.layout.quest_collection_times)

        initCollectionTimesAdapter(savedInstanceState)

	    addTimesButton.setOnClickListener { collectionTimesAdapter.addNew() }

        addOtherAnswer(R.string.quest_collectionTimes_answer_no_times_specified) {
            AlertDialog.Builder(context!!)
                .setTitle(R.string.quest_generic_confirmation_title)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    val answer = Bundle()
                    answer.putBoolean(NO_TIMES_SPECIFIED, true)
                    applyAnswer(answer)
                }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }

        return view
    }

    private fun initCollectionTimesAdapter(savedInstanceState: Bundle?) {
        val data: ArrayList<WeekdaysTimesRow> = if (savedInstanceState != null) {
		    serializer.toObject(savedInstanceState.getByteArray(TIMES_DATA))
	    } else {
		    ArrayList()
	    }

	    collectionTimesAdapter = CollectionTimesAdapter(data, context!!, countryInfo)
        collectionTimesAdapter.registerAdapterDataObserver( AdapterDataChangedWatcher { checkIsFormComplete() })

        collectionTimesList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        collectionTimesList.adapter = collectionTimesAdapter
        collectionTimesList.isNestedScrollingEnabled = false
        checkIsFormComplete()
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

    override fun isFormComplete() = collectionTimesString.isNotEmpty()

    companion object {
        val TIMES = "times"
        val NO_TIMES_SPECIFIED = "no_times_specified"

        private val TIMES_DATA = "times_data"
    }
}
