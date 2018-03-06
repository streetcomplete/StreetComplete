package de.westnordost.streetcomplete.quests.collection_times;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.CountryInfo;
import de.westnordost.streetcomplete.quests.opening_hours.CircularSection;
import de.westnordost.streetcomplete.quests.opening_hours.NumberSystem;
import de.westnordost.streetcomplete.quests.opening_hours.OpeningMonths;
import de.westnordost.streetcomplete.quests.opening_hours.OpeningWeekdays;
import de.westnordost.streetcomplete.quests.opening_hours.TimeRange;
import de.westnordost.streetcomplete.quests.opening_hours.Weekdays;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;
import de.westnordost.streetcomplete.view.dialogs.RangePickerDialog;

public class AddCollectionTimesAdapter extends RecyclerView.Adapter
{
	private final static int MONTHS = 0, WEEKDAYS = 1;

	private ArrayList<OpeningMonths> data;
	private final Context context;
	private final CountryInfo countryInfo;
	private boolean displayMonths = false;

	public AddCollectionTimesAdapter(ArrayList<OpeningMonths> data, Context context, CountryInfo countryInfo)
	{
		this.data = data;
		this.context = context;
		this.countryInfo = countryInfo;
	}

	public String toString()
	{
		return TextUtils.join(", ", data);
	}

	@Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		switch (viewType)
		{
			case MONTHS:
				return new MonthsViewHolder(
						inflater.inflate(R.layout.quest_collection_times_month_row, parent, false));
			case WEEKDAYS:
				return new WeekdayViewHolder(
						inflater.inflate(R.layout.quest_collection_times_weekday_row, parent, false));
		}
		return null;
	}

	@Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
	{
		int[] p = getHierarchicPosition(position);
		OpeningMonths om = data.get(p[0]);

		if(holder instanceof MonthsViewHolder)
		{
			((MonthsViewHolder) holder).update(om, p[0]);
		}
		else if(holder instanceof WeekdayViewHolder)
		{
			OpeningWeekdays ow = om.getWeekdaysList().get(p[1]);
			OpeningWeekdays prevOw = null;
			if(p[1] > 0) prevOw = om.getWeekdaysList().get(p[1] - 1);
			((WeekdayViewHolder) holder).update(ow, prevOw, p[1]);
		}
	}

	@Override public int getItemViewType(int position)
	{
		int[] p = getHierarchicPosition(position);
		return p.length == 1 ? MONTHS : WEEKDAYS;
	}

	private int[] getHierarchicPosition(int position)
	{
		int count = 0;
		for (int i = 0; i < data.size(); ++i)
		{
			OpeningMonths om = data.get(i);
			if(count == position) return new int[]{i};
			++count;

			for (int j = 0; j < om.getWeekdaysList().size(); ++j)
			{
				OpeningWeekdays ow = om.getWeekdaysList().get(j);
				if(count == position) return new int[]{i,j};
				++count;
			}
		}
		throw new IllegalArgumentException();
	}

	@Override public int getItemCount()
	{
		int count = 0;
		for (OpeningMonths om : data)
		{
			count += om.getWeekdaysList().size();
		}
		count += data.size();
		return count;
	}

	/* ------------------------------------------------------------------------------------------ */

	private void remove(int position)
	{
		int[] p = getHierarchicPosition(position);
		if(p.length == 1)
		{
			OpeningMonths om = data.remove(p[0]);
			notifyItemRangeRemoved(position, 1 + om.getWeekdaysList().size());
		}
		else if(p.length == 2)
		{
			ArrayList<OpeningWeekdays> weekdays = data.get(p[0]).getWeekdaysList();
			weekdays.remove(p[1]);
			notifyItemRemoved(position);
			// if not last weekday removed -> element after this one may need to be updated
			// because it may need to show the weekdays now
			if(p[1] < weekdays.size()) notifyItemChanged(position);
		}
	}

	public void addNewMonths()
	{
		openSetMonthsRangeDialog(getMonthsRangeSuggestion(), (startIndex, endIndex) ->
		{
			final CircularSection months = new CircularSection(startIndex, endIndex);
			openSetWeekdaysDialog(getWeekdaysSuggestion(true), weekdays ->
			{
				openSetTimeDialog(getCollectionTimesSuggestion(),
						timeRange -> addMonths(months, weekdays, timeRange));
			});
		});
	}

	private void addMonths(CircularSection months, Weekdays weekdays, TimeRange time)
	{
		int insertIndex = getItemCount();
		data.add(new OpeningMonths(months, new OpeningWeekdays(weekdays, time)));
		notifyItemRangeInserted(insertIndex, 2); // 2 = opening month + opening weekday
	}

	public void addNewWeekdays()
	{
		boolean isFirst = data.get(data.size()-1).getWeekdaysList().isEmpty();
		openSetWeekdaysDialog(getWeekdaysSuggestion(isFirst), (weekdays) ->
		{
			openSetTimeDialog(getCollectionTimesSuggestion(),
					timeRange -> addWeekdays(weekdays, timeRange));
		});
	}

	private void addWeekdays(Weekdays weekdays, TimeRange time)
	{
		int insertIndex = getItemCount();
		data.get(data.size()-1).getWeekdaysList().add(new OpeningWeekdays(weekdays, time));
		notifyItemInserted(insertIndex);
	}

	public ArrayList<OpeningMonths> getData()
	{
		return data;
	}

	public void setDisplayMonths(boolean displayMonths)
	{
		this.displayMonths = displayMonths;
		notifyDataSetChanged();
	}

	public void changeToMonthsMode()
	{
		setDisplayMonths(true);
		final OpeningMonths om = data.get(0);
		openSetMonthsRangeDialog(om.getMonths(), (startIndex, endIndex) ->
		{
			om.setMonths( new CircularSection(startIndex, endIndex) );
			notifyItemChanged(0);
		});
	}

	/* -------------------------------------- months select --------------------------------------*/

	private class MonthsViewHolder extends RecyclerView.ViewHolder
	{
		private TextView monthsText;
		private View delete;

		public MonthsViewHolder(View itemView)
		{
			super(itemView);
			monthsText = itemView.findViewById(R.id.months_from_to);
			delete = itemView.findViewById(R.id.delete);
			delete.setOnClickListener(v ->
			{
				int index = getAdapterPosition();
				if(index != RecyclerView.NO_POSITION) remove(index);
			});
		}

		public void setVisibility(boolean isVisible)
		{
			RecyclerView.LayoutParams param = (RecyclerView.LayoutParams)itemView.getLayoutParams();
			if (isVisible)
			{
				param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
				param.width = LinearLayout.LayoutParams.MATCH_PARENT;
				itemView.setVisibility(View.VISIBLE);
			}
			else
			{
				itemView.setVisibility(View.GONE);
				param.height = 0;
				param.width = 0;
			}
			itemView.setLayoutParams(param);
		}

		public void update(final OpeningMonths data, int index)
		{
			setVisibility(displayMonths);
			delete.setVisibility(index==0 ? View.GONE : View.VISIBLE);
			monthsText.setText(data.getLocalizedMonthsString());
			monthsText.setOnClickListener(v ->
			{
				openSetMonthsRangeDialog(data.getMonths(), (startIndex, endIndex) ->
				{
					data.setMonths( new CircularSection(startIndex, endIndex) );
					notifyItemChanged(getAdapterPosition());
				});
			});
		}
	}

	private @NonNull CircularSection getMonthsRangeSuggestion()
	{
		List<CircularSection> months = getUnmentionedMonths();
		if(months.isEmpty())
		{
			return new CircularSection(0,OpeningMonths.MAX_MONTH_INDEX);
		}
		return months.get(0);
	}

	private List<CircularSection> getUnmentionedMonths()
	{
		List<CircularSection> allTheMonths = new ArrayList<>();
		for (OpeningMonths om : data)
		{
			allTheMonths.add(om.getMonths());
		}
		return new NumberSystem(0,OpeningMonths.MAX_MONTH_INDEX).complemented(allTheMonths);
	}

	private void openSetMonthsRangeDialog(CircularSection months,
										  RangePickerDialog.OnRangeChangeListener callback )
	{
		String[] monthNames = DateFormatSymbols.getInstance().getMonths();
		String selectMonths = context.getResources().getString(R.string.quest_collectionTimes_chooseMonthsTitle);
		new RangePickerDialog(context, callback, monthNames, months.getStart(),
				months.getEnd(), selectMonths).show();
	}

	/* ------------------------------------ weekdays select --------------------------------------*/

	private class WeekdayViewHolder extends RecyclerView.ViewHolder
	{
		private TextView weekdaysText;
		private TextView hoursText;
		private View delete;

		public WeekdayViewHolder(View itemView)
		{
			super(itemView);
			weekdaysText = itemView.findViewById(R.id.weekday_from_to);
			hoursText = itemView.findViewById(R.id.hours_from_to);
			delete = itemView.findViewById(R.id.delete);
			delete.setOnClickListener(v ->
			{
				int index = getAdapterPosition();
				if(index != RecyclerView.NO_POSITION) remove(getAdapterPosition());
			});
		}

		public void update(final OpeningWeekdays data, final OpeningWeekdays previousData, int index)
		{
			delete.setVisibility(index==0 ? View.GONE : View.VISIBLE);

			if(previousData != null && data.weekdays.equals(previousData.weekdays))
			{
				weekdaysText.setText("");
			}
			else
			{
				weekdaysText.setText(data.weekdays.toLocalizedString(context.getResources()));
			}

			weekdaysText.setOnClickListener(v ->
			{
				openSetWeekdaysDialog(data.weekdays, weekdays ->
				{
					data.weekdays = weekdays;
					notifyItemChanged(getAdapterPosition());
				});
			});
			hoursText.setText(data.timeRange.toStringUsing("–"));
			hoursText.setOnClickListener(v ->
			{
				openSetTimeDialog(data.timeRange, timeRange ->
				{
					data.timeRange = timeRange;
					notifyItemChanged(getAdapterPosition());
				});
			});
		}
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

	private static int modulus(int a, int b)
	{
		return (a % b + b) % b;
	}

	private void openSetWeekdaysDialog(final Weekdays weekdays, final WeekdaysPickedListener callback)
	{
		final boolean[] selection = weekdays.getSelection();

		AlertDialog dlg = new AlertDialogBuilder(context)
				.setTitle(R.string.quest_collectionTimes_chooseWeekdaysTitle)
				.setMultiChoiceItems(Weekdays.getNames(context.getResources()), selection,
						(dialog, which, isChecked) -> updateDialogOkButtonEnablement((AlertDialog) dialog, selection))
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok,
						(dialog, which) -> callback.onWeekdaysPicked(new Weekdays(selection)))
				.show();

		updateDialogOkButtonEnablement(dlg, selection);
	}

	private void updateDialogOkButtonEnablement(AlertDialog dlg, boolean[] selection)
	{
		boolean isAnyChecked = false;
		for(boolean b : selection) isAnyChecked |= b;
		dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isAnyChecked);
	}

	private interface WeekdaysPickedListener
	{
		void onWeekdaysPicked(Weekdays selected);
	}

	/* ------------------------------------- times select ----------------------------------------*/

	// this could go into per-country localization when this page (or any other source)
	// https://en.wikipedia.org/wiki/Shopping_hours contains more information about typical
	// opening hours per country
	private static final TimeRange TYPICAL_COLLECTION_TIMES = new TimeRange(8 * 60, 18 * 60, false);

	private void openSetTimeDialog(TimeRange timeRange,
										TimePickerDialog.OnTimeChangeListener callback)
	{
		new TimePickerDialog(context, callback, timeRange).show();
	}

	private @NonNull
	TimeRange getCollectionTimesSuggestion()
	{
		return TYPICAL_COLLECTION_TIMES;
	}
}
