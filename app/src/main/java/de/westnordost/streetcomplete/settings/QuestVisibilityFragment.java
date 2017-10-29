package de.westnordost.streetcomplete.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import javax.inject.Inject;
import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;

public class QuestVisibilityFragment extends Fragment
{
	@Inject QuestVisibilityAdapter questVisibilityAdapter;

	@Override public View onCreateView(LayoutInflater inflater,
									   ViewGroup container, Bundle savedInstanceState)
	{
		Injector.instance.getApplicationComponent().inject(this);

		View view = inflater.inflate(R.layout.fragment_quest_selection, container, false);

		RecyclerView questSelectionList = view.findViewById(R.id.questSelectionList);
		questSelectionList.addItemDecoration(new DividerItemDecoration(
				getContext(), DividerItemDecoration.VERTICAL));
		questSelectionList.setLayoutManager(new LinearLayoutManager(getContext()));
		questSelectionList.setAdapter(questVisibilityAdapter);

		return view;
	}

	@Override public void onStart()
	{
		super.onStart();
		getActivity().setTitle(getString(R.string.pref_title_quests));
	}
}
