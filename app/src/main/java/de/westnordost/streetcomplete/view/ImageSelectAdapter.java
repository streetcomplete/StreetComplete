package de.westnordost.streetcomplete.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.westnordost.streetcomplete.R;

public class ImageSelectAdapter extends RecyclerView.Adapter<ImageSelectAdapter.ViewHolder>
{
	private int selectedIndex = -1;
	private ArrayList<Item> items;

	public interface OnItemSelectedListener
	{
		void onItemSelected(int index);
	}
	private OnItemSelectedListener onItemSelectedListener;
	public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener)
	{
		this.onItemSelectedListener = onItemSelectedListener;
	}

	public void setItems(List<Item> items)
	{
		this.items = new ArrayList<>(items);
		notifyDataSetChanged();
	}

	public void add(Collection<Item> items)
	{
		int len = this.items.size();
		this.items.addAll(items);
		notifyItemRangeInserted(len, items.size());
	}

	@Override public ImageSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).
				inflate(R.layout.labeled_image_select_cell, parent, false);
		return new ImageSelectAdapter.ViewHolder(view);
	}

	public void setSelectedIndex(int index)
	{
		Integer prevSelectedIndex = selectedIndex;
		if(prevSelectedIndex == -1 || prevSelectedIndex != index)
		{
			selectedIndex = index;
		} else {
			selectedIndex = -1;
		}

		if(prevSelectedIndex != -1) notifyItemChanged(prevSelectedIndex);
		if(selectedIndex != -1) notifyItemChanged(selectedIndex);
		if(onItemSelectedListener != null)
		{
			onItemSelectedListener.onItemSelected(selectedIndex);
		}
	}

	@Override public void onBindViewHolder(ImageSelectAdapter.ViewHolder holder, int position)
	{
		Item item = items.get(position);
		holder.imageView.setImageResource(item.drawableId);
		holder.itemView.setSelected(selectedIndex != -1 && position == selectedIndex);
		if(item.titleId > -1) holder.textView.setText(item.titleId);
		else holder.textView.setText("");
	}

	@Override public int getItemCount()
	{
		if(items == null) return 0;
		return items.size();
	}

	/** @return -1 if nothing selected, otherwise the index */
	public int getSelectedIndex()
	{
		return selectedIndex;
	}

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		ImageView imageView;
		TextView textView;

		public ViewHolder(View v)
		{
			super(v);
			imageView = (ImageView) itemView.findViewById(R.id.imageView);
			imageView.setOnClickListener(this);
			textView = (TextView) itemView.findViewById(R.id.textView);
		}

		@Override public void onClick(View v)
		{
			setSelectedIndex(getAdapterPosition());
		}
	}

	public static class Item
	{
		public final int titleId;
		public final int drawableId;

		public Item(int drawableId, int titleId)
		{
			this.drawableId = drawableId;
			this.titleId = titleId;
		}
	}
}
