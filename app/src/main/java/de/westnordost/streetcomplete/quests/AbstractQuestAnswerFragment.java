package de.westnordost.streetcomplete.quests;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.BottomSheetBehavior;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestGroup;

/** Abstract base class for any dialog with which the user answers a specific quest(ion) */
public abstract class AbstractQuestAnswerFragment extends Fragment
{
	public static final String ELEMENT = "element";

	private TextView title;
	private ViewGroup content;

	private View view;

	private QuestAnswerComponent questAnswerComponent;

	protected Button buttonOk;
	protected Button buttonOtherAnswers;
	private ImageButton buttonClose;

	public AbstractQuestAnswerFragment()
	{
		super();
		questAnswerComponent = new QuestAnswerComponent();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.bottom_sheet_fragment, container, false);

		view.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
		{
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom,
									   int oldLeft, int oldTop, int oldRight, int oldBottom)
			{
				// not immediately because this is called during layout change (view.getTop() == 0)
				final Handler handler = new Handler();
				handler.post(new Runnable()
				{
					@Override public void run()
					{
						updateCloseButtonVisibility();
					}
				});
			}
		});

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

		buttonClose = (ImageButton) view.findViewById(R.id.close_btn);
		buttonClose.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				getActivity().onBackPressed();
			}
		});

		BottomSheetBehavior.from(view).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
		{
			@Override public void onStateChanged(@NonNull View bottomSheet, int newState) { }

			@Override public void onSlide(@NonNull View bottomSheet, float slideOffset)
			{
				updateCloseButtonVisibility();
			}
		});

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
						popup.getMenu().add(Menu.NONE, otherAnswer, otherAnswers.size()-i, otherAnswer);
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

	private void updateCloseButtonVisibility()
	{
		// this is called asynchronously. It may happen that the activity is already gone when this
		// method is finally called
		if(getActivity() == null) return;

		int toolbarHeight = getActivity().findViewById(R.id.toolbar).getHeight();
		boolean coversToolbar = view.getTop() < toolbarHeight;
		buttonClose.setVisibility(coversToolbar ? View.VISIBLE : View.GONE);
	}

	@Override public void onCreate(Bundle inState)
	{
		super.onCreate(inState);
		questAnswerComponent.onCreate(getArguments());
	}

	@Override public void onAttach(Context ctx)
	{
		super.onAttach(ctx);
		questAnswerComponent.onAttach((OsmQuestAnswerListener) ctx);
	}

	@Override public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		questAnswerComponent.onAttach((OsmQuestAnswerListener) activity);
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

	/** Apply an answer given in the form with the "OK" button */
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

	/** Request to close the form through user interaction (back button, clicked other quest,..),
	 *  requires user confirmation if any changes have been made */
	@UiThread public void onClickClose(final Runnable confirmed)
	{
		if (!hasChanges())
		{
			confirmed.run();
		}
		else
		{
			DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					confirmed.run();
				}
			};
			new AlertDialog.Builder(getActivity())
					.setMessage(R.string.confirmation_discard_title)
					.setPositiveButton(R.string.confirmation_discard_positive, onYes)
					.setNegativeButton(R.string.confirmation_discard_negative, null)
					.show();
		}
	}

	/** Apply an answer not given through the usual "OK" button (Does not check if the form is empty) */
	protected final void applyOtherAnswer(Bundle data)
	{
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

	protected final void setTitle(String string)
	{
		title.setText(string);
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

	protected final OsmElement getOsmElement()
	{
		return (OsmElement) getArguments().getSerializable(AbstractQuestAnswerFragment.ELEMENT);
	}
}
