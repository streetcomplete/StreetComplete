package de.westnordost.streetcomplete.quests.bench_backrest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddBenchBackrestForm extends YesNoQuestAnswerFragment {
    public static final String PICNIC_TABLE = "picnic_table";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        addOtherAnswers();
        return view;
    }

    private void addOtherAnswers()
    {
        addOtherAnswer(R.string.quest_bench_answer_picnic_table, () -> {
            Bundle answer = new Bundle();
            answer.putBoolean(PICNIC_TABLE, true);
            applyAnswer(answer);
        });
    }
}

