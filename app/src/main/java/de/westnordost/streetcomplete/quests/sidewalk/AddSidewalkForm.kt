package de.westnordost.streetcomplete.quests.sidewalk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.view.ListAdapter
import kotlinx.android.synthetic.main.quest_street_side_puzzle.*

class AddSidewalkForm : AbstractQuestFormAnswerFragment<SidewalkAnswer>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle
    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null
    private var leftSide: Sidewalk? = null
    private var rightSide: Sidewalk? = null

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.getString(SIDEWALK_RIGHT)?.let { rightSide = Sidewalk.valueOf(it) }
        savedInstanceState?.getString(SIDEWALK_LEFT)?.let { leftSide = Sidewalk.valueOf(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        puzzleView.listener = { isRight -> showSidewalkSelectionDialog(isRight) }

        streetSideRotater = StreetSideRotater(puzzleView, compassNeedle, elementGeometry)

        val defaultResId =
            if (isLeftHandTraffic) R.drawable.ic_sidewalk_unknown_l
            else                   R.drawable.ic_sidewalk_unknown

        puzzleView.setLeftSideImageResource(leftSide?.iconResId ?: defaultResId)
        puzzleView.setRightSideImageResource(rightSide?.iconResId ?: defaultResId)

        checkIsFormComplete()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        rightSide?.let { outState.putString(SIDEWALK_RIGHT, it.name) }
        leftSide?.let { outState.putString(SIDEWALK_LEFT, it.name) }
    }

    @AnyThread
    override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    override fun onClickOk() {
        applyAnswer(SidewalkAnswer(
            left = leftSide == Sidewalk.YES,
            right = rightSide == Sidewalk.YES
        ))
    }

    override fun isFormComplete() = leftSide != null && rightSide != null

    override fun isRejectingClose() = leftSide != null || rightSide != null

    private fun showSidewalkSelectionDialog(isRight: Boolean) {
        val recyclerView = RecyclerView(activity!!)
        recyclerView.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        recyclerView.layoutManager = GridLayoutManager(activity, 2)

        val alertDialog = AlertDialog.Builder(activity!!)
            .setTitle(R.string.quest_select_hint)
            .setView(recyclerView)
            .create()

        recyclerView.adapter = createAdapter(Sidewalk.values().toList()) { sidewalk ->
            alertDialog.dismiss()

            if (isRight) {
                puzzleView.replaceRightSideImageResource(sidewalk.puzzleResId)
                rightSide = sidewalk
            } else {
                puzzleView.replaceLeftSideImageResource(sidewalk.puzzleResId)
                leftSide = sidewalk
            }
            checkIsFormComplete()
        }
        alertDialog.show()
    }

    private fun createAdapter(items: List<Sidewalk>, callback: (Sidewalk) -> Unit) =
        object : ListAdapter<Sidewalk>(items) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : ListAdapter.ViewHolder<Sidewalk>(
                    LayoutInflater.from(parent.context).inflate(R.layout.labeled_icon_button_cell, parent, false)
                ) {
                    override fun onBind(with: Sidewalk) {
                        val imageView = itemView.findViewById<ImageView>(R.id.imageView)
                        val textView = itemView.findViewById<TextView>(R.id.textView)
                        imageView.setImageDrawable(resources.getDrawable(with.iconResId))
                        textView.setText(with.nameResId)
                        itemView.setOnClickListener { callback(with) }
                    }
                }
        }

    private enum class Sidewalk(val iconResId: Int, val puzzleResId: Int, val nameResId: Int) {
        NO(R.drawable.ic_sidewalk_no, R.drawable.ic_sidewalk_puzzle_no, R.string.quest_sidewalk_value_no),
        YES(R.drawable.ic_sidewalk_yes, R.drawable.ic_sidewalk_puzzle_yes, R.string.quest_sidewalk_value_yes)
    }

    companion object {
        private const val SIDEWALK_LEFT = "sidewalk_left"
        private const val SIDEWALK_RIGHT = "sidewalk_right"
    }
}
