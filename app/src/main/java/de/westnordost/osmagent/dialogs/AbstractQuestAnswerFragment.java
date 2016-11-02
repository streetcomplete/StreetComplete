package de.westnordost.osmagent.dialogs;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmagent.R;
import de.westnordost.osmagent.data.QuestGroup;

/** Abstract base class for any dialog with which the user answers a specific quest(ion) */
public abstract class AbstractQuestAnswerFragment extends Fragment
{
	private TextView title;
	private ViewGroup content;

	private QuestAnswerComponent questAnswerComponent;

	protected Button buttonOk;
	protected Button buttonOtherAnswers;

	public AbstractQuestAnswerFragment()
	{
		super();
		questAnswerComponent = new QuestAnswerComponent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.bottom_sheet_fragment, container, false);

		title = (TextView) view.findViewById(R.id.title);
		buttonOk = (Button) view.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickOk();
			}
		});

		buttonOtherAnswers = (Button) view.findViewById(R.id.buttonOtherAnswers);

		final List<Integer> otherAnswers = getOtherAnswerResourceIds();
		if(otherAnswers.isEmpty())
		{
			buttonOtherAnswers.setVisibility(View.INVISIBLE);
		}
		else if(otherAnswers.size() == 1)
		{
			buttonOtherAnswers.setText(otherAnswers.get(0));
			buttonOtherAnswers.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					onClickOtherAnswer(otherAnswers.get(0));
				}
			});
		}
		else
		{
			buttonOtherAnswers.setText(R.string.quest_generic_otherAnswers);
			buttonOtherAnswers.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					PopupMenu popup = new PopupMenu(getActivity(), buttonOtherAnswers);
					for(int i = 0; i<otherAnswers.size(); ++i)
					{
						int otherAnswer = otherAnswers.get(i);
						MenuItem item = popup.getMenu().add(Menu.NONE, otherAnswer, i, otherAnswer);
					}
					popup.show();

					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
					{
						@Override public boolean onMenuItemClick(MenuItem item)
						{
							return onClickOtherAnswer(item.getItemId());
						}
					});
				}
			});
		}

		content = (ViewGroup) view.findViewById(R.id.content);

		return view;
	}

	@Override public void onCreate(Bundle inState)
	{
		super.onCreate(inState);
		questAnswerComponent.onCreate(getArguments());
	}

	@Override
	public void onAttach(Context ctx)
	{
		super.onAttach(ctx);
		questAnswerComponent.onAttach((OsmQuestAnswerListener) ctx);
	}

	protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = new ArrayList<>();
		answers.add(R.string.quest_generic_answer_notApplicable);
		return answers;
	}

	protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if(itemResourceId == R.string.quest_generic_answer_notApplicable)
		{
			onClickCantSay();
			return true;
		}
		return false;
	}

	protected final void onClickCantSay()
	{
		DialogFragment leaveNote = new LeaveNoteDialog();
		leaveNote.setArguments(questAnswerComponent.getArguments());
		leaveNote.show(getFragmentManager(), null);
	}

	protected abstract void onClickOk();

	protected final void applyAnswer(Bundle data)
	{
		// each form should check this on its own, but in case it doesn't, this is the last chance
		if(!hasChanges())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}
		questAnswerComponent.onAnswerQuest(data);
	}

	protected final void skipQuest()
	{
		questAnswerComponent.onSkippedQuest();
	}

	protected final void setTitle(int resourceId)
	{
		title.setText(resourceId);
	}

	protected final View setContentView(int resourceId)
	{
		return getActivity().getLayoutInflater().inflate(resourceId, content);
	}

	public abstract boolean hasChanges();

	public final long getQuestId()
	{
		return questAnswerComponent.getQuestId();
	}

	public final QuestGroup getQuestGroup()
	{
		return questAnswerComponent.getQuestGroup();
	}
}
