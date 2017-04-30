package de.westnordost.streetcomplete.quests.bus_stop_shelter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddBusStopShelterForm extends AbstractQuestAnswerFragment
{
    public static final String BUS_STOP_SHELTER = "bus_stop_shelter";

    private Button buttonYes;
    private Button buttonNo;
    private boolean buttonClicked = false;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setTitle(R.string.quest_busStopShelter_title);

        view.findViewById(R.id.buttonOk).setVisibility(View.GONE);
        view.findViewById(R.id.buttonOtherAnswers).setVisibility(View.GONE);

        View contentView = setContentView(R.layout.quest_generic_hasfeature);

        buttonYes = (Button) view.findViewById(R.id.buttonHasFeatureYes);
        buttonYes.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                onClickYesNo(true);
            }
        });

        buttonNo = (Button) view.findViewById(R.id.buttonHasFeatureNo);
        buttonNo.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                onClickYesNo(false);
            }
        });

        return view;
	}

	private void onClickYesNo(boolean hasFeature) {
        Bundle answer = new Bundle();
        answer.putString(BUS_STOP_SHELTER, hasFeature ? "yes" : "no");
        applyAnswer(answer);
        buttonClicked = true;
    }

    @Override protected void onClickOk() {
        onClickYesNo(true);
    }

    @Override public boolean hasChanges()
    {
        return buttonClicked;
    }
}