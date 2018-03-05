package de.westnordost.streetcomplete.quests.collection_times;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.util.Serializer;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

import static android.view.Menu.NONE;

public class AddCollectionTimesForm extends AbstractQuestFormAnswerFragment
{
	public static final String COLLECTION_TIMES = "collection_times";

	private static final String	COLLECTION_TIMES_DATA = "ct_data",
								IS_ADD_MONTHS_MODE = "ct_add_months";

	private boolean isAlsoAddingMonths;
	private AddCollectionTimesAdapter collectionTimesAdapter;

	@Inject Serializer serializer;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		addOtherAnswers();

		View contentView = setContentView(R.layout.quest_collection_times);

		initCollectionTimesAdapter(contentView, savedInstanceState);

		Button addTimes = contentView.findViewById(R.id.btn_add);
		addTimes.setOnClickListener(this::onClickAddButton);

		return view;
	}

	private void initCollectionTimesAdapter(View contentView, Bundle savedInstanceState)
	{
		ArrayList<OpeningMonths> data;
		if(savedInstanceState != null)
		{
			data = serializer.toObject(savedInstanceState.getByteArray(COLLECTION_TIMES_DATA),ArrayList.class);
			isAlsoAddingMonths = savedInstanceState.getBoolean(IS_ADD_MONTHS_MODE);
		}
		else
		{
			data = new ArrayList<>();
			data.add(new OpeningMonths());
			isAlsoAddingMonths = false;
		}

		collectionTimesAdapter = new AddCollectionTimesAdapter(data, getActivity(), getCountryInfo());
		collectionTimesAdapter.setDisplayMonths(isAlsoAddingMonths);
		RecyclerView collectionTimesList = contentView.findViewById(R.id.collection_times_list);
		collectionTimesList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		collectionTimesList.setAdapter(collectionTimesAdapter);
		collectionTimesList.setNestedScrollingEnabled(false);
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_collectionTimes_answer_no_regular_collection_times, this::showInputCommentDialog);
		addOtherAnswer(R.string.quest_collectionTimes_answer_seasonal_collection_times, this::changeToMonthsMode);
	}

	private void onClickAddButton(View v)
	{
		if(!isAlsoAddingMonths)
		{
			collectionTimesAdapter.addNewWeekdays();
		}
		else
		{
			PopupMenu m = new PopupMenu(getActivity(), v);
			m.getMenu().add(NONE,0,NONE,R.string.quest_collectionTimes_add_weekdays);
			m.getMenu().add(NONE,1,NONE,R.string.quest_collectionTimes_add_months);
			m.setOnMenuItemClickListener(item ->
			{
				if(0 == item.getItemId()) collectionTimesAdapter.addNewWeekdays();
				else if(1 == item.getItemId()) collectionTimesAdapter.addNewMonths();
				return true;
			});
			m.show();
		}
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putByteArray(COLLECTION_TIMES_DATA, serializer.toBytes(collectionTimesAdapter.getData()));
		outState.putBoolean(IS_ADD_MONTHS_MODE, isAlsoAddingMonths);
	}

	@Override protected void onClickOk()
	{
		applyCollectionTimes(collectionTimesAdapter.toString());
	}

	private void showInputCommentDialog()
	{
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.quest_collection_times_comment, null);
		final EditText editText = view.findViewById(R.id.commentInput);

		new AlertDialogBuilder(getContext())
				.setTitle(R.string.quest_collectionTimes_comment_title)
				.setView(view)
				.setPositiveButton(android.R.string.ok, (dialog, which) ->
				{
					String txt = editText.getText().toString().replaceAll("\"","").trim();
					if(txt.isEmpty())
					{
						new AlertDialogBuilder(getContext())
								.setMessage(R.string.quest_collectionTimes_emptyAnswer)
								.setPositiveButton(R.string.ok, null)
								.show();
						return;
					}

					Bundle answer = new Bundle();
					answer.putString(COLLECTION_TIMES, "\""+txt+"\"");
					applyImmediateAnswer(answer);
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	private void changeToMonthsMode()
	{
		isAlsoAddingMonths = true;
		collectionTimesAdapter.changeToMonthsMode();
	}

	private void applyCollectionTimes(String collectionTimes)
	{
		Bundle answer = new Bundle();
		answer.putString(COLLECTION_TIMES, collectionTimes);
		applyFormAnswer(answer);
	}

	@Override public boolean hasChanges()
	{
		return !collectionTimesAdapter.toString().isEmpty();
	}

}
