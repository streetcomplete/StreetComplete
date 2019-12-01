package de.westnordost.streetcomplete.quests.opening_hours

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.PopupMenu
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText

import java.util.ArrayList

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.opening_hours.adapter.AddOpeningHoursAdapter
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.util.Serializer


import android.view.Menu.NONE
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.ktx.toObject
import kotlinx.android.synthetic.main.quest_opening_hours.*

class AddOpeningHoursForm : OpeningHoursForm<OpeningHoursAnswer>() {
    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onClickOk() {
        applyAnswer(RegularOpeningHours(openingHoursAdapter.createOpeningMonths()))
    }

    override fun showInputCommentDialog() {
        val view = LayoutInflater.from(activity).inflate(R.layout.quest_opening_hours_comment, null)
        val commentInput = view.findViewById<EditText>(R.id.commentInput)

        AlertDialog.Builder(context!!)
                .setTitle(R.string.quest_openingHours_comment_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val txt = commentInput.text.toString().replace("\"","").trim()
                    if (txt.isEmpty()) {
                        AlertDialog.Builder(context!!)
                                .setMessage(R.string.quest_openingHours_emptyAnswer)
                                .setPositiveButton(R.string.ok, null)
                                .show()
                    } else {
                        applyAnswer(DescribeOpeningHours(txt))
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    override fun showConfirm24_7Dialog() {
        AlertDialog.Builder(activity!!)
                .setMessage(R.string.quest_openingHours_24_7_confirmation)
                .setPositiveButton(android.R.string.yes) { _, _ -> applyAnswer(AlwaysOpen) }
                .setNegativeButton(android.R.string.no, null)
                .show()
    }

    override fun confirmNoSign() {
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.quest_generic_confirmation_title)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoOpeningHoursSign) }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
    }

}
