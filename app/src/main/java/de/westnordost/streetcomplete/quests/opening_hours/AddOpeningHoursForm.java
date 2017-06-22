package de.westnordost.streetcomplete.quests.opening_hours;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.CountryInfo;
import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.util.Serializer;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

import static android.view.Menu.NONE;

public class AddOpeningHoursForm extends AbstractQuestFormAnswerFragment
{
	public static final String OPENING_HOURS = "opening_hours";

	private static final String	OPENING_HOURS_DATA = "oh_data",
								IS_ADD_MONTHS_MODE = "oh_add_months";

	private boolean isAlsoAddingMonths;
	private AddOpeningHoursAdapter openingHoursAdapter;

	@Inject Serializer serializer;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		setTitle();

		View contentView = setContentView(R.layout.quest_opening_hours);

		ArrayList<OpeningMonths> data;
		if(savedInstanceState != null)
		{
			data = serializer.toObject(savedInstanceState.getByteArray(OPENING_HOURS_DATA),ArrayList.class);
			isAlsoAddingMonths = savedInstanceState.getBoolean(IS_ADD_MONTHS_MODE);
		}
		else
		{
			data = new ArrayList<>();
			data.add(new OpeningMonths());
			isAlsoAddingMonths = false;
		}

		openingHoursAdapter = new AddOpeningHoursAdapter(data, getActivity(), getCountryInfo());
		openingHoursAdapter.setDisplayMonths(isAlsoAddingMonths);
		RecyclerView openingHoursList = (RecyclerView) contentView.findViewById(R.id.opening_hours_list);
		openingHoursList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		openingHoursList.setAdapter(openingHoursAdapter);
		openingHoursList.setNestedScrollingEnabled(false);

		Button addTimes = (Button) contentView.findViewById(R.id.btn_add);
		addTimes.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAddButton(v);
			}
		});

		return view;
	}

	private void onClickAddButton(View v)
	{
		if(!isAlsoAddingMonths)
		{
			openingHoursAdapter.addNewWeekdays();
		}
		else
		{
			PopupMenu m = new PopupMenu(getActivity(), v);
			m.getMenu().add(NONE,0,NONE,R.string.quest_openingHours_add_weekdays);
			m.getMenu().add(NONE,1,NONE,R.string.quest_openingHours_add_months);
			m.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
			{
				@Override public boolean onMenuItemClick(MenuItem item)
				{
					if(0 == item.getItemId()) openingHoursAdapter.addNewWeekdays();
					else if(1 == item.getItemId()) openingHoursAdapter.addNewMonths();
					return true;
				}
			});
			m.show();
		}
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putByteArray(OPENING_HOURS_DATA, serializer.toBytes(openingHoursAdapter.getData()));
		outState.putBoolean(IS_ADD_MONTHS_MODE, isAlsoAddingMonths);
	}

	private void setTitle()
	{
		OsmElement element = getOsmElement();
		String name = null;
		if(element != null && element.getTags() != null)
		{
			name = element.getTags().get("name");
		}
		setTitle(R.string.quest_openingHours_name_title, name);
	}

	@Override protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = super.getOtherAnswerResourceIds();
		answers.add(R.string.quest_openingHours_answer_no_regular_opening_hours);
		answers.add(R.string.quest_openingHours_answer_247);
		answers.add(R.string.quest_openingHours_answer_seasonal_opening_hours);
		return answers;
	}

	@Override protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if(super.onClickOtherAnswer(itemResourceId)) return true;

		if(itemResourceId == R.string.quest_openingHours_answer_247)
		{
			showConfirm24_7Dialog();
			return true;
		}
		if(itemResourceId == R.string.quest_openingHours_answer_seasonal_opening_hours)
		{
			isAlsoAddingMonths = true;
			openingHoursAdapter.changeToMonthsMode();
			return true;
		}
		if(itemResourceId == R.string.quest_openingHours_answer_no_regular_opening_hours)
		{
			showInputCommentDialog();
			return true;
		}

		return false;
	}

	@Override protected void onClickOk()
	{
		applyOpeningHours(openingHoursAdapter.toString());
	}

	private void showInputCommentDialog()
	{
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.quest_opening_hours_comment, null);
		final EditText editText = (EditText)view.findViewById(R.id.commentInput);

		new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_openingHours_comment_title)
				.setView(view)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						String txt = editText.getText().toString().replaceAll("\"","");
						Bundle answer = new Bundle();
						answer.putString(OPENING_HOURS, "\""+txt+"\"");
						applyImmediateAnswer(answer);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	private void showConfirm24_7Dialog()
	{
		new AlertDialogBuilder(getActivity())
				.setMessage(R.string.quest_openingHours_24_7_confirmation)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						Bundle answer = new Bundle();
						answer.putString(OPENING_HOURS, "24/7");
						applyImmediateAnswer(answer);
					}
				})
				.setNegativeButton(android.R.string.no, null)
				.show();
	}

	private void applyOpeningHours(String openingHours)
	{
		Bundle answer = new Bundle();
		answer.putString(OPENING_HOURS, openingHours);
		applyFormAnswer(answer);
	}

	@Override public boolean hasChanges()
	{
		return !openingHoursAdapter.toString().isEmpty();
	}

}
