package de.westnordost.streetcomplete.view

import android.content.Context
import android.content.res.Resources
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ViewTimeRestrictionSelectBinding
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningHoursAdapter
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningHoursRow
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Allows to input a time restriction, either inclusive or exclusive, based on opening hours. */
class TimeRestrictionSelectView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewTimeRestrictionSelectBinding.inflate(LayoutInflater.from(context), this)

    private val hoursAdapter = OpeningHoursAdapter(context)

    private val timeRestrictionAdapter = ArrayAdapter(
        context,
        R.layout.spinner_item_centered,
        TimeRestriction.values().map { it.toLocalizedString(context.resources) }.toMutableList()
    )

    var onInputChanged: (() -> Unit)? = null

    var firstDayOfWorkweek: String
        set(value) { hoursAdapter.firstDayOfWorkweek = value }
        get() = hoursAdapter.firstDayOfWorkweek

    var regularShoppingDays: Int
        set(value) { hoursAdapter.regularShoppingDays = value }
        get() = hoursAdapter.regularShoppingDays

    var timeRestriction: TimeRestriction
        set(value) { binding.selectAtHours.setSelection(selectableTimeRestrictions.indexOf(value)) }
        get() = selectableTimeRestrictions[binding.selectAtHours.selectedItemPosition]

    var selectableTimeRestrictions: List<TimeRestriction> = TimeRestriction.values().toList()
        set(value) {
            field = value
            timeRestrictionAdapter.clear()
            timeRestrictionAdapter.addAll(value.map { it.toLocalizedString(context.resources) })
        }

    var hours: List<OpeningHoursRow>
        set(value) { hoursAdapter.rows = value.toMutableList() }
        get() = hoursAdapter.rows

    val isComplete: Boolean get() =
        timeRestriction == TimeRestriction.AT_ANY_TIME || hours.isNotEmpty()

    init {
        hoursAdapter.registerAdapterDataObserver(AdapterDataChangedWatcher { onInputChanged?.invoke() })
        binding.openingHoursList.adapter = hoursAdapter
        binding.addTimesButton.setOnClickListener { hoursAdapter.addNewWeekdays() }

        binding.selectAtHours.adapter = timeRestrictionAdapter
        if (binding.selectAtHours.selectedItemPosition < 0) binding.selectAtHours.setSelection(0)
        binding.openingHoursContainer.isGone = timeRestriction == TimeRestriction.AT_ANY_TIME
        binding.selectAtHours.onItemSelectedListener = OnAdapterItemSelectedListener {
            binding.openingHoursContainer.isGone = timeRestriction == TimeRestriction.AT_ANY_TIME
            onInputChanged?.invoke()
        }
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.oh = hoursAdapter.rows
        return ss
    }

    public override fun onRestoreInstanceState(s: Parcelable) {
        val ss = s as SavedState
        super.onRestoreInstanceState(ss.superState)
        ss.oh?.let { hoursAdapter.rows = it }
    }

    internal class SavedState : BaseSavedState {
        var oh: MutableList<OpeningHoursRow>? = null

        constructor(superState: Parcelable?) : super(superState)
        constructor(parcel: Parcel) : super(parcel) {
            oh = parcel.readString()?.let { Json.decodeFromString(it) }
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(Json.encodeToString(oh))
        }

        companion object {
            @JvmField @Keep
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
                override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
            }
        }
    }
}

enum class TimeRestriction { AT_ANY_TIME, ONLY_AT_HOURS, EXCEPT_AT_HOURS }

private fun TimeRestriction.toLocalizedString(resources: Resources) = when (this) {
    TimeRestriction.AT_ANY_TIME -> resources.getString(R.string.at_any_time)
    TimeRestriction.ONLY_AT_HOURS -> resources.getString(R.string.only_at_hours)
    TimeRestriction.EXCEPT_AT_HOURS -> resources.getString(R.string.except_at_hours)
}
