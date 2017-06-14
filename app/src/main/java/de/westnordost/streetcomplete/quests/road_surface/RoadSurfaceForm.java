package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.GroupedImageSelectAdapter;

public abstract class RoadSurfaceForm extends AbstractQuestFormAnswerFragment {
	public static final String SURFACE = "surface";
	private GroupedImageSelectAdapter imageSelector;

	abstract Surface[] GetSurfaceMenuStructure();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_streetSurface_title);

		View contentView = setContentView(R.layout.quest_street_surface);

		RecyclerView surfaceSelect = (RecyclerView) contentView.findViewById(R.id.surfaceSelect);
		imageSelector = new GroupedImageSelectAdapter(Arrays.<GroupedImageSelectAdapter.Item>asList(GetSurfaceMenuStructure()));
		surfaceSelect.setAdapter(imageSelector);
		surfaceSelect.setNestedScrollingEnabled(false);

		return view;
	}

	@Override
	protected void onClickOk()
	{
		Bundle answer = new Bundle();
		if (getSelectedSurface() != null) {
			answer.putString(SURFACE, getSelectedSurface().value);
		}
		applyFormAnswer(answer);
	}

	@Override
	public boolean hasChanges()
	{
		return getSelectedSurface() != null;
	}

	private Surface getSelectedSurface()
	{
		return (Surface) imageSelector.getSelectedItem();
	}

	protected static class Surface extends GroupedImageSelectAdapter.Item {
		public final String value;

		public Surface(String value, int drawableId, int titleId)
		{
			super(drawableId, titleId);
			this.value = value;
		}

		public Surface(String value, int drawableId, int titleId, GroupedImageSelectAdapter.Item[] items)
		{
			super(drawableId, titleId, items);
			this.value = value;
		}
	}
}
