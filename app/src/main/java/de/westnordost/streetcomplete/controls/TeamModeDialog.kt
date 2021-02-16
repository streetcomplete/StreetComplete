package de.westnordost.streetcomplete.controls

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.dialog_team_mode.view.*
import java.util.concurrent.CopyOnWriteArrayList

/** Shows a dialog containing the team mode settings */
class TeamModeDialog(
    context: Context,
    onEnableTeamMode: (Int, Int) -> Unit
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {
    private var selectedTeamSize: Int? = null
    private var selectedIndexInTeam: Int? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_team_mode, null)

        val adapter = ColorCirclesSelectAdapter()
        adapter.listeners.add(object : ColorCirclesSelectAdapter.OnSelectedIndexChangedListener {
            override fun onSelectedIndexChanged(index: Int?) {
                selectedIndexInTeam = index
                getButton(BUTTON_POSITIVE).isEnabled = index != null
            }
        })
        view.color_circles.adapter = adapter
        view.color_circles.layoutManager = GridLayoutManager(context, 3)

        view.team_size_input.addTextChangedListener { editable ->
            selectedTeamSize = parseTeamSize(editable.toString())

            if (selectedTeamSize === null) {
                view.team_size_hint.isGone = false
                view.color_hint.isGone = true
                view.color_circles.isGone = true
            } else {
                view.team_size_hint.isGone = true
                view.color_hint.isGone = false
                view.color_circles.isGone = false
                adapter.count = selectedTeamSize!!
            }
        }

        setButton(BUTTON_POSITIVE, context.resources.getText(android.R.string.ok)) { _, _ ->
            onEnableTeamMode(selectedTeamSize!!, selectedIndexInTeam!!)
            dismiss()
        }

        setOnShowListener {
            getButton(BUTTON_POSITIVE).isEnabled = false
        }

        setView(view)
    }

    private fun parseTeamSize(string: String): Int? {
        return try {
            val number = Integer.parseInt(string)
            if (number in 2..TeamModeColorCircle.maxTeamSize) number else null
        } catch (e: NumberFormatException) { null }
    }
}

class ColorCirclesSelectAdapter : RecyclerView.Adapter<ColorCircleViewHolder>() {
    var count: Int = 0
        set(value) {
            deselect()
            field = value
            notifyDataSetChanged()
        }

    private var selectedIndex: Int? = null
        set(index) {
            val oldIndex = field
            field = index

            oldIndex?.let { notifyItemChanged(it) }
            index?.let { notifyItemChanged(it) }
        }

    val listeners: MutableList<OnSelectedIndexChangedListener> = CopyOnWriteArrayList()

    interface OnSelectedIndexChangedListener {
        fun onSelectedIndexChanged(index: Int?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorCircleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(TeamModeColorCircle.layout, parent, false)
        val holder = ColorCircleViewHolder(view)
        holder.onClickListener = ::toggle
        return holder
    }

    private fun toggle(index: Int) {
        if (index < 0 || index >= count)
            throw ArrayIndexOutOfBoundsException(index)

        selectedIndex = if (index == selectedIndex) null else index

        for (listener in listeners) {
            listener.onSelectedIndexChanged(selectedIndex)
        }
    }

    private fun deselect() {
        selectedIndex?.let { toggle(it) }
    }

    override fun onBindViewHolder(holder: ColorCircleViewHolder, position: Int) {
        holder.bind(position)
        holder.itemView.isSelected = selectedIndex == position
    }

    override fun getItemCount() = count
}

class ColorCircleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var onClickListener: ((index: Int) -> Unit)? = null
        set(value) {
            field = value
            if (value == null) itemView.setOnClickListener(null)
            else itemView.setOnClickListener {
                val index = adapterPosition
                if (index != RecyclerView.NO_POSITION) value.invoke(index)
            }
        }

    fun bind(index: Int) {
        TeamModeColorCircle.setViewColorsAndText(itemView, index)
    }
}
