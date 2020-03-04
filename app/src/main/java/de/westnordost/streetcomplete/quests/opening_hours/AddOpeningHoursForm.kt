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

class AddOpeningHoursForm : OpeningHoursForm() {
    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onClickOk() {
        applyAnswer(RegularOpeningHours(openingHoursAdapter.createOpeningMonths()))
    }


    override fun isFormComplete() = openingHoursAdapter.createOpeningMonths().joinToString(";").isNotEmpty()

    companion object {
        private const val OPENING_HOURS_DATA = "oh_data"
        private const val IS_ADD_MONTHS_MODE = "oh_add_months"
    }
}
