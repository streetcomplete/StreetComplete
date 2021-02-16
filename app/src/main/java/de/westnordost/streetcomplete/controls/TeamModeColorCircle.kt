package de.westnordost.streetcomplete.controls

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import de.westnordost.streetcomplete.R

object TeamModeColorCircle {

    const val layout = R.layout.cell_team_mode_color_circle_select

    // "background color" to "text color"
    private val teamModeColors = listOf(
        Color.parseColor("#e45f5f") to Color.parseColor("#550000"), // red
        Color.parseColor("#9bbe55") to Color.parseColor("#2d5016"), // green
        Color.parseColor("#529add") to Color.parseColor("#002255"), // blue
        Color.parseColor("#ca72e2") to Color.parseColor("#440055"), // magenta
        Color.parseColor("#ffdd55") to Color.parseColor("#554400"), // yellow
        Color.parseColor("#aaaaaa") to Color.parseColor("#333333"), // light grey
        Color.parseColor("#f4900c") to Color.parseColor("#552200"), // orange
        Color.parseColor("#495aad") to Color.parseColor("#d5e5ff"), // dark blue
        Color.parseColor("#5a8049") to Color.parseColor("#d5ffe6"), // dark green
        Color.parseColor("#aa3333") to Color.parseColor("#ffd5d5"), // dark red
        Color.parseColor("#646464") to Color.parseColor("#ececec"), // dark grey
        Color.parseColor("#a07a53") to Color.parseColor("#f0e8e0"), // brown
    )

    val maxTeamSize get() = teamModeColors.size

    fun setViewColorsAndText(view: View, indexInTeam: Int) {
        if (indexInTeam !in teamModeColors.indices) {
            throw IndexOutOfBoundsException("Team index is not in range 0..${maxTeamSize - 1}")
        }

        val imageView = view.findViewById<ImageView>(R.id.teamModeColorCircleBackground)
        val textView = view.findViewById<TextView>(R.id.teamModeColorCircleText)

        val (backgroundColor, textColor) = teamModeColors[indexInTeam]
        imageView.setColorFilter(backgroundColor)
        textView.text = (indexInTeam + 'A'.toInt()).toChar().toString()
        textView.setTextColor(textColor)
    }
}
