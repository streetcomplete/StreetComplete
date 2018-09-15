package de.westnordost.streetcomplete.quests;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osmnotes.AbstractCreateNoteFragment;

public class LeaveNoteInsteadFragment extends AbstractCreateNoteFragment
{
	public static final String ARG_QUEST_TITLE = "questTitle";

	private QuestAnswerComponent questAnswerComponent;
	private String questTitle;

	public LeaveNoteInsteadFragment()
	{
		super();
		questAnswerComponent = new QuestAnswerComponent();
	}

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.map_btn_create_note);
		setDescription(R.string.quest_leave_new_note_description);

		return view;
	}

	@Override public void onCreate(Bundle inState)
	{
		super.onCreate(inState);
		questAnswerComponent.onCreate(getArguments());
		questTitle = getArguments().getString(ARG_QUEST_TITLE);
	}

	@Override
	public void onAttach(Context ctx)
	{
		super.onAttach(ctx);
		questAnswerComponent.onAttach((OsmQuestAnswerListener) ctx);
	}

	@Override protected void onLeaveNote(String text, @Nullable ArrayList<String> imagePaths)
	{
		questAnswerComponent.onLeaveNote(questTitle, text, imagePaths);
	}

	@Override protected int getLayoutResId() { return R.layout.fragment_quest_answer; }
}
