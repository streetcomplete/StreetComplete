package de.westnordost.streetcomplete.quests;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.util.TextChangedWatcher;

public abstract class TextInputQuestAnswerFragment extends AbstractQuestFormAnswerFragment
{
	public static final String INPUT = "input";

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		getEditText().addTextChangedListener(new TextChangedWatcher(this::checkIsFormComplete));
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		answer.putString(INPUT, getInputString());
		applyAnswer(answer);
	}

	@Override public boolean isFormComplete() { return !getInputString().isEmpty(); }

	private String getInputString() { return getEditText().getText().toString(); }

	protected abstract EditText getEditText();
}
