package de.westnordost.streetcomplete.quests.postbox_collection_times;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.CountryInfo;
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays;
import de.westnordost.streetcomplete.quests.opening_hours.WeekdaysPickerDialog;

public class CollectionTimesAdapter extends RecyclerView.Adapter<CollectionTimesAdapter.WeekdayViewHolder>
{
	private ArrayList<WeekdaysTimes> data;
	private final Context context;
	private final CountryInfo countryInfo;

	public CollectionTimesAdapter(ArrayList<WeekdaysTimes> data, Context context, CountryInfo countryInfo)
	{
		this.data = data;
		this.context = context;
		this.countryInfo = countryInfo;
	}

	public String toString()
	{
		StringBuilder result = new StringBuilder();
		boolean firstDays = true;

		Weekdays lastWeekdays = null;
		for (WeekdaysTimes times : data)
		{
			boolean isSameWeekdays = lastWeekdays != null && lastWeekdays.equals(times.weekdays);
			if(!isSameWeekdays)
			{
				if(!firstDays)	result.append(", ");
				else            firstDays = false;

				result.append(times.weekdays.toString());
				result.append(" ");
			}
			else
			{
				result.append(",");
			}
			result.append(timeOfDayToString(times.minutes));

			lastWeekdays = times.weekdays;
		}

		return result.toString();
	}

	@Override public WeekdayViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new WeekdayViewHolder(
				inflater.inflate(R.layout.quest_times_weekday_row, parent, false));
	}

	@Override public void onBindViewHolder(WeekdayViewHolder holder, int position)
	{
		WeekdaysTimes times = data.get(position);
		WeekdaysTimes previousTimes = null;
		if(position > 0) previousTimes = data.get(position - 1);
		holder.update(times, previousTimes);
	}

	@Override public int getItemCount()
	{
		return data.size();
	}

	/* ------------------------------------------------------------------------------------------ */

	private void remove(int position)
	{
		data.remove(position);
		notifyItemRemoved(position);
		// if not last weekday removed -> element after this one may need to be updated
		// because it may need to show the weekdays now
		if(position < data.size()) notifyItemChanged(position);
	}

	public void addNew()
	{
		boolean isFirst = data.isEmpty();
		openSetWeekdaysDialog(getWeekdaysSuggestion(isFirst), (weekdays) ->
		{
			openSetTimeDialog(12 * 60,	(minutes) -> add(weekdays, minutes));
		});
	}

	private void add(Weekdays weekdays, int minutes)
	{
		int insertIndex = getItemCount();
		data.add(new WeekdaysTimes(weekdays, minutes));
		notifyItemInserted(insertIndex);
	}

	public ArrayList<WeekdaysTimes> getData()
	{
		return data;
	}

	/* ------------------------------------ weekdays select --------------------------------------*/

	 class WeekdayViewHolder extends RecyclerView.ViewHolder
	{
		private TextView weekdaysText;
		private TextView hoursText;
		private View delete;

		public WeekdayViewHolder(View itemView)
		{
			super(itemView);
			weekdaysText = itemView.findViewById(R.id.weekday);
			hoursText = itemView.findViewById(R.id.hours);
			delete = itemView.findViewById(R.id.delete);
			delete.setOnClickListener(v ->
			{
				int index = getAdapterPosition();
				if(index != RecyclerView.NO_POSITION) remove(getAdapterPosition());
			});
		}

		public void update(final WeekdaysTimes times, final WeekdaysTimes previousTimes)
		{
			if(previousTimes != null && times.weekdays.equals(previousTimes.weekdays))
			{
				weekdaysText.setText("");
			}
			else
			{
				weekdaysText.setText(times.weekdays.toLocalizedString(context.getResources()));
			}

			weekdaysText.setOnClickListener(v ->
			{
				openSetWeekdaysDialog(times.weekdays, weekdays ->
				{
					times.weekdays = weekdays;
					notifyItemChanged(getAdapterPosition());
				});
			});
			hoursText.setText(timeOfDayToString(times.minutes));
			hoursText.setOnClickListener(v ->
			{
				openSetTimeDialog(times.minutes, minutes ->
				{
					times.minutes = minutes;
					notifyItemChanged(getAdapterPosition());
				});
			});
		}
	}

	@SuppressLint("DefaultLocale") private static String timeOfDayToString(int minutes)
	{
		return String.format("%02d:%02d", minutes / 60, minutes % 60);
	}

	private @NonNull Weekdays getWeekdaysSuggestion(boolean isFirst)
	{
		if(isFirst)
		{
			int firstWorkDayIdx = Weekdays.getWeekdayIndex(countryInfo.getFirstDayOfWorkweek());
			boolean[] result = new boolean[7];
			for(int i = 0; i < countryInfo.getRegularShoppingDays(); ++i)
			{
				result[(i + firstWorkDayIdx) % 7] = true;
			}
			return new Weekdays(result);
		}
		return new Weekdays();
	}

	private void openSetWeekdaysDialog(Weekdays weekdays, WeekdaysPickerDialog.OnWeekdaysPickedListener callback)
	{
		WeekdaysPickerDialog.show(context, weekdays, callback);
	}

	private interface TimePickedListener { void onTimePicked(int minutes); }

	private void openSetTimeDialog(int minutes, TimePickedListener callback)
	{
		new TimePickerDialog(context, R.style.Theme_Bubble_Dialog,
			(view, hourOfDay, minute) -> callback.onTimePicked(hourOfDay * 60 + minute),
			minutes / 60, minutes % 60,	true).show();
	}
}
