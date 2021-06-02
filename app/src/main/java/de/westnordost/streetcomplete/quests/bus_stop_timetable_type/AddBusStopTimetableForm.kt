package de.westnordost.streetcomplete.quests.bus_stop_timetable_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.quests.bus_stop_timetable_type.BusStopTimetable.*

class AddBusStopTimetableForm : AImageListQuestAnswerFragment<BusStopTimetable, BusStopTimetable>() {

    override val items = listOf(
        Item(PRINTED,      R.drawable.timetable_type_printed,     R.string.quest_busStopTimetable_printed),
        Item(DELAY,  R.drawable.timetable_type_delay, R.string.quest_busStopTimetable_delay),
        Item(REALTIME,         R.drawable.timetable_type_realtime,        R.string.quest_busStopTimetable_realtime),
    )

    override val itemsPerRow = 3

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_busStopTimetable_none) { applyAnswer(NONE) }
    )

    override fun onClickOk(selectedItems: List<BusStopTimetable>) {
        applyAnswer(selectedItems.single())
    }
}
