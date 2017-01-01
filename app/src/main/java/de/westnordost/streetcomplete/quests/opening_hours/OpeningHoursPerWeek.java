package de.westnordost.streetcomplete.quests.opening_hours;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.CurrentCountry;
import de.westnordost.streetcomplete.data.meta.WorkWeek;
import de.westnordost.streetcomplete.util.SerializedSavedState;

public class OpeningHoursPerWeek extends LinearLayout implements OpeningHoursFormRoot
{
	@Inject CurrentCountry currentCountry;

	private Map<OpeningHoursPerDay, Weekdays> ranges = new HashMap<>();

	private Button btnAdd;
	private ViewGroup rows;

	public OpeningHoursPerWeek(Context context)
	{
		super(context);
		init();
	}

	public OpeningHoursPerWeek(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	private void init()
	{
		Injector.instance.getApplicationComponent().inject(this);

		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.list_with_bottom_add_btn, this, true);

		btnAdd = (Button) findViewById(R.id.btn_add);
		btnAdd.setText(R.string.quest_openingHours_add_weekdays);
		btnAdd.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				add();
			}
		});
		rows = (ViewGroup) findViewById(R.id.rows);
	}

	/** Open dialog to let the user specify the range and add this new row*/
	public void add()
	{
		openSetWeekdaysDialog(getWeekdaysSuggestion(), new WeekdaysPickedListener()
		{
			@Override public void onWeekdaysPicked(Weekdays selected)
			{
				add(selected).add();
			}
		});
	}

	/** add a new row with the given range */
	public OpeningHoursPerDay add(Weekdays weekdays)
	{
		final LayoutInflater inflater = LayoutInflater.from(getContext());
		final LinearLayout row = (LinearLayout) inflater.inflate(
				R.layout.quest_opening_hours_weekday_row, rows, false);

		final View btnDelete = row.findViewById(R.id.delete);
		final OpeningHoursPerDay openingHoursPerDay =
				(OpeningHoursPerDay) row.findViewById(R.id.hours_select_container);
		final TextView fromTo = (TextView) row.findViewById(R.id.weekday_from_to);

		btnDelete.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				remove(row);
			}
		});

		openingHoursPerDay.setOnOpeningTimesDefinedListener(
				new OpeningHoursPerDay.OnOpeningTimesDefinedListener()
				{
					@Override public void onOpeningTimesDefined(boolean defined)
					{
						btnDelete.setVisibility(defined ? GONE : VISIBLE);
					}
				});

		fromTo.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				Weekdays weekdays = ranges.get(openingHoursPerDay);
				openSetWeekdaysDialog(weekdays, new WeekdaysPickedListener()
				{
					@Override public void onWeekdaysPicked(Weekdays weekdays)
					{
						putData(fromTo, openingHoursPerDay, weekdays);
					}
				});
			}
		});

		putData(fromTo, openingHoursPerDay, weekdays);
		rows.addView(row);
		return openingHoursPerDay;
	}

	@Override public Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		return new SerializedSavedState(superState, getAll());
	}

	@Override public void onRestoreInstanceState(Parcelable state)
	{
		SerializedSavedState savedState = (SerializedSavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		addAll((HashMap) savedState.get(HashMap.class));
	}

	@Override protected void dispatchSaveInstanceState(SparseArray<Parcelable> container)
	{
		super.dispatchFreezeSelfOnly(container);
	}

	@Override protected void dispatchRestoreInstanceState(SparseArray container)
	{
		super.dispatchThawSelfOnly(container);
	}

	public HashMap<Weekdays, ArrayList<TimeRange>> getAll()
	{
		HashMap<Weekdays, ArrayList<TimeRange>> result = new HashMap<>(2);
		for (Map.Entry<OpeningHoursPerDay, Weekdays> e : ranges.entrySet())
		{
			result.put(e.getValue(), e.getKey().getAll());
		}
		return result;
	}

	public void addAll(HashMap<Weekdays, ArrayList<TimeRange>> data)
	{
		ArrayList<Weekdays> sortedKeys = new ArrayList<>(data.keySet());
		Collections.sort(sortedKeys);
		for(Weekdays weekdays : sortedKeys)
		{
			OpeningHoursPerDay dayView = add(weekdays);
			dayView.addAll(data.get(weekdays));
		}
	}

	public void remove(ViewGroup view)
	{
		OpeningHoursPerDay child =	(OpeningHoursPerDay) view.findViewById(R.id.hours_select_container);
		ranges.remove(child);
		rows.removeView(view);
	}

	private @NonNull Weekdays getWeekdaysSuggestion()
	{
		// first entry
		if(ranges.isEmpty())
		{
			Locale locale = currentCountry.getLocale();

			int firstWorkDayIdx = modulus(WorkWeek.getFirstDay(locale) - 2,7);
			boolean[] result = new boolean[7];
			for(int i = 0; i < WorkWeek.getRegularShoppingDays(locale); ++i)
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

		AlertDialog dlg = new AlertDialog.Builder(getContext())
				.setTitle(R.string.quest_openingHours_chooseWeekdaysTitle)
				.setMultiChoiceItems(Weekdays.getNames(getResources()), selection, new DialogInterface.OnMultiChoiceClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which, boolean isChecked)
					{
						updateDialogOkButtonEnablement((AlertDialog) dialog, selection);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						callback.onWeekdaysPicked(new Weekdays(selection));
					}
				})
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

	@Override public String getOpeningHoursString()
	{
		return getOpeningHoursString("");
	}

	public String getOpeningHoursString(String prepend)
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for(Map.Entry<OpeningHoursPerDay, Weekdays> entry : ranges.entrySet())
		{
			String hourlyOpeningHours = entry.getKey().getOpeningHoursString();

			// while it is legal syntax to specify opening hours without actual opening hours (just the
			// days), this form deliberately does not accept it. If it is really open the whole day
			// and night, the user must select the time 0-24
			if(hourlyOpeningHours.isEmpty()) continue;

			Weekdays selection = entry.getValue();

			if(!first)	result.append("; ");
			else		first = false;

			result.append(prepend);
			result.append(selection.toString());
			result.append(" ");
			result.append(hourlyOpeningHours);
		}

		return result.toString();
	}

	private void putData(TextView view, OpeningHoursPerDay openingHoursPerDay, Weekdays selected)
	{
		view.setText(selected.toLocalizedString(getResources()));
		ranges.put(openingHoursPerDay, selected);
	}
}
