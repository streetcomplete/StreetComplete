package de.westnordost.streetcomplete.quests.roof_shape

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddRoofShapeForm : ImageListQuestAnswerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        imageSelector.setCellLayout(R.layout.cell_labeled_icon_select)

        addOtherAnswer(R.string.quest_roofShape_answer_many) { applyManyRoofsAnswer() }

        return view
    }

    private fun applyManyRoofsAnswer() {
        val answer = Bundle()
        val strings = ArrayList<String>(1)
        strings.add("many")
        answer.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, strings)
        applyAnswer(answer)
    }

    override fun getMaxNumberOfInitiallyShownItems() = 8

    override fun getItems() = arrayOf(
        Item("gabled", R.drawable.ic_roof_gabled),
        Item("hipped", R.drawable.ic_roof_hipped),
        Item("flat", R.drawable.ic_roof_flat),
        Item("pyramidal", R.drawable.ic_roof_pyramidal),

        Item("half-hipped", R.drawable.ic_roof_half_hipped),
        Item("skillion", R.drawable.ic_roof_skillion),
        Item("gambrel", R.drawable.ic_roof_gambrel),
        Item("round", R.drawable.ic_roof_round),

        Item("double_saltbox", R.drawable.ic_roof_double_saltbox),
        Item("saltbox", R.drawable.ic_roof_saltbox),
        Item("mansard", R.drawable.ic_roof_mansard),
        Item("dome", R.drawable.ic_roof_dome),

        Item("quadruple_saltbox", R.drawable.ic_roof_quadruple_saltbox),
        Item("round_gabled", R.drawable.ic_roof_round_gabled),
        Item("onion", R.drawable.ic_roof_onion),
        Item("cone", R.drawable.ic_roof_cone)
    )
}
