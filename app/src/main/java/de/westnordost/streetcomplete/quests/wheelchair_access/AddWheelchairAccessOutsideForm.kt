package de.westnordost.streetcomplete.quests.wheelchair_access

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.westnordost.streetcomplete.R

class AddWheelchairAccessOutsideForm : WheelchairAccessAnswerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setContentView(R.layout.quest_wheelchair_outside_explanation)
        return view
    }
}
