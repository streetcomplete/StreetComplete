package de.westnordost.streetcomplete.quests.smoothness

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestGenericListBinding
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.asItem
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.asImageSpan
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class AddSmoothnessForm : AImageListQuestForm<Smoothness, SmoothnessAnswer>() {

    private val binding by contentViewBinding(QuestGenericListBinding::bind)

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_smoothness_wrong_surface) { surfaceWrong() },
        createConvertToStepsAnswer(),
        AnswerItem(R.string.quest_smoothness_obstacle) { showObstacleHint() }
    )

    private val surfaceTag get() = element.tags["surface"]

    override val items get() = Smoothness.values().toItems(requireContext(), surfaceTag!!)

    override val itemsPerRow = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_smoothness
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val description = context.getString(R.string.quest_smoothness_hint)
        val stringBuilder = SpannableStringBuilder(description)
        stringBuilder.replaceEmojiWithImageSpan(context, "🚲", R.drawable.ic_smoothness_city_bike)
        stringBuilder.replaceEmojiWithImageSpan(context, "🚗", R.drawable.ic_smoothness_car)
        stringBuilder.replaceEmojiWithImageSpan(context, "🚙", R.drawable.ic_smoothness_suv)

        binding.descriptionLabel.isGone = false
        binding.descriptionLabel.text = stringBuilder
    }

    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<Smoothness>) {
        applyAnswer(SmoothnessValueAnswer(selectedItems.single()))
    }

    private fun showObstacleHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_smoothness_obstacle_hint)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    private fun surfaceWrong() {
        val surfaceType = Surface.values().find { it.osmValue == surfaceTag }!!
        showWrongSurfaceDialog(surfaceType)
    }

    private fun showWrongSurfaceDialog(surface: Surface) {
        val inflater = LayoutInflater.from(requireContext())
        val inner = inflater.inflate(R.layout.dialog_quest_smoothness_wrong_surface, null, false)
        ItemViewHolder(inner.findViewById(R.id.item_view)).bind(surface.asItem())

        AlertDialog.Builder(requireContext())
            .setView(inner)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes_leave_note) { _, _ -> composeNote() }
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> applyAnswer(WrongSurfaceAnswer) }
            .show()
    }

    private fun createConvertToStepsAnswer(): AnswerItem? {
        return if (element.couldBeSteps()) {
            AnswerItem(R.string.quest_generic_answer_is_actually_steps) {
                applyAnswer(IsActuallyStepsAnswer)
            }
        } else null
    }
}

private fun SpannableStringBuilder.replaceEmojiWithImageSpan(
    context: Context,
    emoji: String,
    @DrawableRes drawableResId: Int
) {
    val iconDrawable = context.getDrawable(drawableResId) ?: return
    val index = this.indexOf(emoji)
    this.setSpan(
        iconDrawable.asImageSpan(36, 36),
        index,
        index + emoji.length,
        Spannable.SPAN_INCLUSIVE_INCLUSIVE
    )
}
