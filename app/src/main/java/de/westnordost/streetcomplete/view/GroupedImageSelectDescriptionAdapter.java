package de.westnordost.streetcomplete.view;

import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;

public class GroupedImageSelectDescriptionAdapter extends GroupedImageSelectAdapter
{
	public GroupedImageSelectDescriptionAdapter(GridLayoutManager gridLayoutManager)
	{
		gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
		{
			@Override public int getSpanSize(int position)
			{
				return items.get(position).isGroup() ? 2 : 1;
			}
		});
	}

	@Override public GroupedImageSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		int layoutId = viewType == GROUP ?
			R.layout.cell_panorama_select : R.layout.cell_labeled_image_select_with_description;
		View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
		return new ViewHolder(view);
	}
}
