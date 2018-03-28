package de.westnordost.streetcomplete.quests.localized_name;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddBusStopNameForm extends AddLocalizedNameForm
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_localizedname);

		addOtherAnswers();

		initLocalizedNameAdapter(contentView, savedInstanceState);

		return view;
	}

	@Override
	protected void onClickOk() {
		this.applyNameAnswer();
	}

	protected void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_name_answer_noName, this::confirmNoName);
		addOtherAnswer(R.string.quest_streetName_answer_cantType, this::showKeyboardInfo);
	}

	@Override
	protected void initLocalizedNameAdapter(View contentView, Bundle savedInstanceState)
	{
		ArrayList<LocalizedName> data;
		if(savedInstanceState != null)
		{
			data = serializer.toObject(savedInstanceState.getByteArray(LOCALIZED_NAMES_DATA),ArrayList.class);
		}
		else
		{
			data = new ArrayList<>();
		}

		Button addLanguageButton = contentView.findViewById(R.id.btn_add);

		adapter = new AddLocalizedNameAdapter(
			data, getActivity(), getPossibleStreetsignLanguages(),
			null, null, addLanguageButton);
		RecyclerView recyclerView = contentView.findViewById(R.id.roadnames);
		recyclerView.setLayoutManager(
			new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		recyclerView.setAdapter(adapter);
		recyclerView.setNestedScrollingEnabled(false);
	}

	@Override
	protected void applyNameAnswer()
	{
		Bundle bundle = new Bundle();
		ArrayList<LocalizedName> data = adapter.getData();

		String[] names = new String[data.size()];
		String[] languageCodes = new String[data.size()];
		for (int i = 0; i<data.size(); ++i)
		{
			names[i] = data.get(i).name;
			languageCodes[i] = data.get(i).languageCode;
		}

		bundle.putStringArray(NAMES, names);
		bundle.putStringArray(LANGUAGE_CODES, languageCodes);
		applyFormAnswer(bundle);
	}

	private void confirmNoName()
	{
		new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_name_answer_noName_confirmation_title)
				.setPositiveButton(R.string.quest_name_noName_confirmation_positive, (dialog, which) ->
				{
					Bundle data = new Bundle();
					data.putBoolean(NO_NAME, true);
					applyImmediateAnswer(data);
				})
				.setNegativeButton(R.string.quest_generic_confirmation_no, null)
				.show();
	}

	@Override public boolean hasChanges()
	{
		// either the user added a language or typed something for the street name
		return adapter.getData().size() > 1 || !adapter.getData().get(0).name.trim().isEmpty();
	}
}
