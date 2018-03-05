package de.westnordost.streetcomplete.quests.collection_times;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

import static android.view.Menu.NONE;

public class TimePickerDialog extends AlertDialog implements View.OnClickListener
{
	private final TimePicker timePicker;
	//private final LinearLayout tabLayout;

	private final OnTimeChangeListener listener;

	public interface OnTimeChangeListener
	{
		void onTimeRangeChange(TimeRange time);
	}

	public TimePickerDialog(Context context, OnTimeChangeListener listener,	TimeRange time)
	{
		super(context, R.style.AppTheme_AlertDialog);

		this.listener = listener;

		final LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.dialog_time_picker, null);
		setView(view);

		setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), (OnClickListener) null);
		setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), (OnClickListener) null);

		timePicker = view.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);

		if(time != null)
		{
			timePicker.setCurrentHour(time.getStart() / 60);
			timePicker.setCurrentMinute(time.getStart() % 60);
		}
	}

	private class CustomAdapter extends PagerAdapter
	{
		private final CharSequence[] labels;

		public CustomAdapter(CharSequence startTimeLabel, CharSequence endTimeLabel)
		{
			labels = new CharSequence[] {startTimeLabel, endTimeLabel};
		}

		@Override public int getCount()
		{
			return labels.length;
		}

		@Override public boolean isViewFromObject(View view, Object object)
		{
			return view == timePicker;
		}

		@Override public void destroyItem(ViewGroup container, int position, Object object)
		{
			container.removeView(timePicker);
		}

		@Override public Object instantiateItem(ViewGroup container, int position)
		{
			container.addView(timePicker);
			return position;
		}

		@Override public CharSequence getPageTitle(int position)
		{
			return labels[position];
		}
	}

	@Override public void show()
	{
		super.show();
		// to override the default OK=dismiss() behavior
		getButton(BUTTON_POSITIVE).setOnClickListener(this);
	}

	@Override public void onClick(View v)
	{
		applyAndDismiss();
	}

	private void applyAndDismiss()
	{
		if (listener != null)
		{
			listener.onTimeRangeChange(
					new TimeRange(getMinutesStart(), getMinutesStart(), false));
		}
		dismiss();
	}

	private int getMinutesStart()
	{
		return timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
	}

	private void onClickAddMonthsButton(View v)
	{
		final boolean[] selection = new boolean[]{false, true, false, true, false, false, false, false};//weekdays.getSelection();

		android.support.v7.app.AlertDialog dlg = new AlertDialogBuilder(getContext())
			.setTitle(R.string.quest_collectionTimes_chooseWeekdaysTitle)
			.setMultiChoiceItems(Weekdays.getNames(getContext().getResources()), selection,null)
			//(dialog, which, isChecked) -> updateDialogOkButtonEnablement((android.support.v7.app.AlertDialog) dialog, selection))
			.setNegativeButton(android.R.string.cancel, null)
		//.setPositiveButton(android.R.string.ok,
			//			(dialog, which) -> callback.onWeekdaysPicked(new Weekdays(selection)))
			.show();

		//updateDialogOkButtonEnablement(dlg, selection);

		//PopupMenu m = new PopupMenu( getContext(), v);
		//m.getMenu().add(NONE,0,NONE,R.string.quest_collectionTimes_add_weekdays);
		//m.getMenu().add(NONE,1,NONE,R.string.quest_collectionTimes_add_months);
		//m.setOnMenuItemClickListener(item ->
			//{
			////if(0 == item.getItemId()) collectionTimesAdapter.addNewWeekdays();
			////else if(1 == item.getItemId()) collectionTimesAdapter.addNewMonths();
			//return true;
			//});
		//m.show();
	}

	private void onClickAddWeekdaysButton(View v)
	{
		PopupMenu m = new PopupMenu(getContext(), v);
		m.getMenu().add(NONE,0,NONE,R.string.quest_collectionTimes_add_weekdays);
		m.getMenu().add(NONE,1,NONE,R.string.quest_collectionTimes_add_months);
		m.setOnMenuItemClickListener(item ->
		{
			//if(0 == item.getItemId()) collectionTimesAdapter.addNewWeekdays();
			//else if(1 == item.getItemId()) collectionTimesAdapter.addNewMonths();
			return true;
		});
		m.show();
	}
}
