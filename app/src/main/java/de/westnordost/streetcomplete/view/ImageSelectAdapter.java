package de.westnordost.streetcomplete.view;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.westnordost.streetcomplete.R;

public class ImageSelectAdapter extends RecyclerView.Adapter<ImageSelectAdapter.ViewHolder>
{
	private Integer selectedIndex;
	private ArrayList<Drawable> items;

	public ImageSelectAdapter(List<Drawable> items)
	{
		this.items = new ArrayList<>(items);
	}

	public void add(Collection<Drawable> items)
	{
		int len = this.items.size();
		this.items.addAll(items);
		notifyItemRangeInserted(len, items.size());
	}

	@Override public ImageSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).
				inflate(R.layout.image_select_cell, parent, false);
		return new ImageSelectAdapter.ViewHolder(view);
	}

	private void onSelect(int index)
	{
		Integer prevSelectedIndex = selectedIndex;
		if(prevSelectedIndex == null || prevSelectedIndex != index)
		{
			selectedIndex = index;
		} else {
			selectedIndex = null;
		}

		if(prevSelectedIndex != null) notifyItemChanged(prevSelectedIndex);
		if(selectedIndex != null) notifyItemChanged(selectedIndex);
	}

	@Override public void onBindViewHolder(ImageSelectAdapter.ViewHolder holder, int position)
	{
		holder.imageView.setImageDrawable(items.get(position));
		holder.itemView.setSelected(selectedIndex != null && position == selectedIndex);
	}

	@Override public int getItemCount()
	{
		return items.size();
	}

	/** @return null if nothing selected, otherwise the index */
	public Integer getSelectedIndex()
	{
		return selectedIndex;
	}

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		ImageView imageView;

		public ViewHolder(View v)
		{
			super(v);
			imageView = (ImageView) itemView;
			imageView.setOnClickListener(this);
		}

		@Override public void onClick(View v)
		{
			onSelect(getAdapterPosition());
		}
	}
}
