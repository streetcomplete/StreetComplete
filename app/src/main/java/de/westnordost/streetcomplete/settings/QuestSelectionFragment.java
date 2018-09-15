package de.westnordost.streetcomplete.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderList;
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeDao;


public class QuestSelectionFragment extends Fragment
{
	@Inject QuestSelectionAdapter questSelectionAdapter;

	@Inject QuestTypeRegistry questTypeRegistry;
	@Inject VisibleQuestTypeDao visibleQuestTypeDao;
	@Inject QuestTypeOrderList questTypeOrderList;

	@Override public View onCreateView(LayoutInflater inflater,
									   ViewGroup container, Bundle savedInstanceState)
	{
		Injector.instance.getApplicationComponent().inject(this);

		setHasOptionsMenu(true);

		View view = inflater.inflate(R.layout.fragment_quest_selection, container, false);

		questSelectionAdapter.setList(createQuestTypeVisibilityList());

		RecyclerView questSelectionList = view.findViewById(R.id.questSelectionList);
		questSelectionList.addItemDecoration(new DividerItemDecoration(
				getContext(), DividerItemDecoration.VERTICAL));
		questSelectionList.setLayoutManager(new LinearLayoutManager(getContext()));
		questSelectionList.setAdapter(questSelectionAdapter);

		return view;
	}

	@Override public void onStart()
	{
		super.onStart();
		getActivity().setTitle(getString(R.string.pref_title_quests));
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_quest_selection, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.action_reset:
				new AlertDialog.Builder(getContext())
					.setMessage(R.string.pref_quests_reset)
					.setPositiveButton(android.R.string.ok, (dialog, which) -> onReset())
					.setNegativeButton(android.R.string.cancel, null)
					.show();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onReset()
	{
		questTypeOrderList.clear();
		visibleQuestTypeDao.clear();
		questSelectionAdapter.setList(createQuestTypeVisibilityList());
	}


	private List<QuestSelectionAdapter.QuestVisibility> createQuestTypeVisibilityList()
	{
		List<QuestType> questTypes = new ArrayList<>(questTypeRegistry.getAll());
		questTypeOrderList.sort(questTypes);
		List<QuestSelectionAdapter.QuestVisibility> result = new ArrayList<>(questTypes.size());
		for (QuestType questType : questTypes)
		{
			QuestSelectionAdapter.QuestVisibility questVisibility = new QuestSelectionAdapter.QuestVisibility();
			questVisibility.questType = questType;
			questVisibility.visible = visibleQuestTypeDao.isVisible(questType);
			result.add(questVisibility);
		}
		return result;
	}
}
