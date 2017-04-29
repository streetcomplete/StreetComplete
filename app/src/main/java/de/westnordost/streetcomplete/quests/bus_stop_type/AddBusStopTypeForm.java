package de.westnordost.streetcomplete.quests.bus_stop_type;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.view.GroupedImageSelectAdapter;

public class AddBusStopTypeForm extends AbstractQuestAnswerFragment
{
	public static final String BUS_STOP_TYPE = "bus_stop_type";

	private GroupedImageSelectAdapter imageSelector;

	private final BusStopType[] BUS_STOP_TYPES = new BusStopType[]{
			new BusStopType("informal",	R.drawable.bus_stop_informal, R.string.quest_busStopType_value_informal),
			new BusStopType("pole", 	R.drawable.bus_stop_pole, R.string.quest_busStopType_value_pole),
			new BusStopType("shelter",	R.drawable.bus_stop_shelter, R.string.quest_busStopType_value_shelter)
	};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setTitle(R.string.quest_busStopType_title);

        View contentView = setContentView(R.layout.quest_bus_stop_type);

        RecyclerView surfaceSelect = (RecyclerView) contentView.findViewById(R.id.busStopTypeSelect);
        imageSelector = new GroupedImageSelectAdapter(Arrays.<GroupedImageSelectAdapter.Item>asList(BUS_STOP_TYPES));
        surfaceSelect.setAdapter(imageSelector);
        surfaceSelect.setNestedScrollingEnabled(false);

        return view;
	}

    @Override protected void onClickOk()
    {
        Bundle answer = new Bundle();
        if(getSelectedBusStopType() != null)
        {
            answer.putString(BUS_STOP_TYPE, getSelectedBusStopType().value);
        }
        applyAnswer(answer);
    }

    @Override public boolean hasChanges()
    {
        return getSelectedBusStopType() != null;
    }

    private BusStopType getSelectedBusStopType()
    {
        return (BusStopType) imageSelector.getSelectedItem();
    }

    private static class BusStopType extends GroupedImageSelectAdapter.Item
    {
        public final String value;

        public BusStopType(String value, int drawableId, int titleId)
        {
            super(drawableId, titleId);
            this.value = value;
        }

        public BusStopType(String value, int drawableId, int titleId, GroupedImageSelectAdapter.Item[] items)
        {
            super(drawableId, titleId, items);
            this.value = value;
        }
    }
}