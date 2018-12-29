package de.westnordost.streetcomplete.quests.postbox_collection_times;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher;
import de.westnordost.streetcomplete.util.Serializer;


public class AddCollectionTimesForm extends AbstractQuestFormAnswerFragment
{
	public static final String TIMES = "times", NO_TIMES_SPECIFIED = "no_times_specified";

	private static final String TIMES_DATA = "times_data";

	private CollectionTimesAdapter collectionTimesAdapter;

	@Inject Serializer serializer;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		View contentView = setContentView(R.layout.quest_collection_times);

		initCollectionTimesAdapter(contentView, savedInstanceState);

		Button addTimes = contentView.findViewById(R.id.btn_add);
		addTimes.setOnClickListener((v) -> collectionTimesAdapter.addNew());

		addOtherAnswer(R.string.quest_collectionTimes_answer_no_times_specified, () ->
		{
			new AlertDialog.Builder(getContext())
				.setTitle(R.string.quest_generic_confirmation_title)
				.setPositiveButton(R.string.quest_generic_confirmation_yes, (dialog, which) ->
				{
					Bundle answer = new Bundle();
					answer.putBoolean(NO_TIMES_SPECIFIED, true);
					applyAnswer(answer);
				})
				.setNegativeButton(R.string.quest_generic_confirmation_no, null)
				.show();
		});

		return view;
	}

	private void initCollectionTimesAdapter(View contentView, Bundle savedInstanceState)
	{
		ArrayList<WeekdaysTimes> data;
		if(savedInstanceState != null)
		{
			data = serializer.toObject(savedInstanceState.getByteArray(TIMES_DATA),ArrayList.class);
		}
		else
		{
			data = new ArrayList<>();
		}

		collectionTimesAdapter = new CollectionTimesAdapter(data, getContext(), getCountryInfo());
		collectionTimesAdapter.registerAdapterDataObserver(new AdapterDataChangedWatcher(this::checkIsFormComplete));
		RecyclerView collectionTimesList = contentView.findViewById(R.id.collection_times_list);
		collectionTimesList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		collectionTimesList.setAdapter(collectionTimesAdapter);
		collectionTimesList.setNestedScrollingEnabled(false);
		checkIsFormComplete();
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putByteArray(TIMES_DATA, serializer.toBytes(collectionTimesAdapter.getData()));
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		answer.putString(TIMES, collectionTimesAdapter.toString());
		applyAnswer(answer);
	}

	@Override public boolean isFormComplete() { return !collectionTimesAdapter.toString().isEmpty(); }
}
