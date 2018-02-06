package de.westnordost.streetcomplete.view;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.R;


public class GroupedImageSelectAdapter extends RecyclerView.Adapter<GroupedImageSelectAdapter.ViewHolder>
{
	private final static int GROUP = 0;
	private final static int CELL = 1;

	private ArrayList<Item> items = new ArrayList<>();
	private Item selectedItem;

	public GroupedImageSelectAdapter(GridLayoutManager gridLayoutManager)
	{
		gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
		{
			@Override public int getSpanSize(int position)
			{
				return items.get(position).isGroup() ? 3 : 1;
			}
		});
	}

	public void setItems(List<Item> items)
	{
		this.items = new ArrayList<>(items);
		selectedItem = null;
		notifyDataSetChanged();
	}

	@Override public GroupedImageSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		int layoutId = viewType == GROUP ?
				R.layout.cell_panorama_select : R.layout.cell_labeled_image_select;
		View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(final GroupedImageSelectAdapter.ViewHolder viewHolder, final int position)
	{
		Item item = items.get(position);
		final boolean isSelected = selectedItem != null && items.indexOf(selectedItem) == position;

		viewHolder.imageView.setImageResource(item.drawableId);
		viewHolder.textView.setText(item.titleId);
		viewHolder.itemView.setSelected(isSelected);
	}

	public Item getSelectedItem()
	{
		return selectedItem;
	}

	private void onSelect(int index)
	{
		Item prevSelectedItem = selectedItem;
		if(selectedItem == null || prevSelectedItem != items.get(index))
		{
			selectedItem = items.get(index);
		} else {
			selectedItem = null;
		}

		if(selectedItem != null)
		{
			int selectedIndex = items.indexOf(selectedItem);
			notifyItemChanged(selectedIndex);

			if(selectedItem.isGroup())
			{
				if(prevSelectedItem == null || getGroup(items.indexOf(prevSelectedItem)) != selectedIndex )
				{
					expandGroup(selectedIndex);
				}
			}
		}
		if(prevSelectedItem != null)
		{
			int prevSelectedIndex = items.indexOf(prevSelectedItem);
			notifyItemChanged(prevSelectedIndex);

			int previousGroupIndex = getGroup(prevSelectedIndex);
			if(previousGroupIndex != -1)
			{
				if (selectedItem == null || previousGroupIndex != getGroup(items.indexOf(selectedItem)))
				{
					retractGroup(previousGroupIndex);
				}
			}
		}
	}

	private int getGroup(int index)
	{
		for (int i = index; i >= 0; i--)
		{
			if (items.get(i).isGroup()) return i;
		}
		return -1;
	}

	private void expandGroup(int index)
	{
		Item item = items.get(index);
		for (int i = 0; i < item.items.length; i++) {
			items.add(index + i + 1, item.items[i]);
		}
		notifyItemRangeInserted(index + 1, item.items.length);
	}

	private void retractGroup(int index)
	{
		Item item = items.get(index);
		for (int i = 0; i < item.items.length; i++) {
			items.remove(index + 1);
		}
		notifyItemRangeRemoved(index + 1, item.items.length);
	}

	@Override public int getItemCount()
	{
		return items.size();
	}

	@Override public int getItemViewType(int position)
	{
		return items.get(position).isGroup() ? GROUP : CELL;
	}

	public class ViewHolder extends RecyclerView.ViewHolder
	{
		ImageView imageView;
		TextView textView;

		public ViewHolder(View itemView)
		{
			super(itemView);
			imageView = itemView.findViewById(R.id.imageView);
			textView = itemView.findViewById(R.id.textView);
			itemView.setOnClickListener(v ->
			{
				int index = getAdapterPosition();
				if(index != RecyclerView.NO_POSITION) onSelect(getAdapterPosition());
			});
		}
	}
}
