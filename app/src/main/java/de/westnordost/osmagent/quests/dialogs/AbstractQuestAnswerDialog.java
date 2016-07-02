package de.westnordost.osmagent.quests.dialogs;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import de.westnordost.osmagent.R;

/** Abstract base class for any dialog with which the user answers a specific quest(ion) */
public abstract class AbstractQuestAnswerDialog extends AbstractQuestDialog
{
	private TextView title;
	private ViewGroup content;

	protected Button buttonOk;
	protected Button buttonCantSay;
	protected Button buttonCancel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.quest_generic_dialog, container, false);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		title = (TextView) view.findViewById(R.id.title);
		buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickCancel();
			}
		});
		buttonOk = (Button) view.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickOk();
			}
		});
		buttonCantSay = (Button) view.findViewById(R.id.buttonCantSay);
		buttonCantSay.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickCantSay();
			}
		});

		content = (ViewGroup) view.findViewById(R.id.content);

		return view;
	}

	protected void onClickCantSay()
	{
		DialogFragment dialog = new LeaveNoteDialog();
		Bundle args = new Bundle();
		args.putInt(AbstractQuestAnswerDialog.QUEST_ID, questId);
		dialog.setArguments(args);

		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.remove(this);
		transaction.add(dialog, null);
		transaction.commit();
	}

	private void onClickCancel()
	{
		dismiss();
	}

	private void onClickOk()
	{
		if(!validate()) return;
		applyAnswerAndDismiss();
	}

	protected final void applyAnswerAndDismiss()
	{
		Bundle data = new Bundle();
		addAnswer(data);
		callbackListener.onAnsweredQuest(questId, data);
		dismiss();
	}

	protected abstract void addAnswer(Bundle data);

	protected boolean validate()
	{
		return true;
	}

	protected final void setTitle(int resourceId)
	{
		title.setText(resourceId);
	}

	protected final View setContentView(int resourceId)
	{
		return getActivity().getLayoutInflater().inflate(resourceId, content);
	}

}
