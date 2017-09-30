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

	private final ArrayList<Item> data;
	private Item selectedItem;

	public GroupedImageSelectAdapter(List<Item> data)
	{
		this.data = new ArrayList<>(data);
	}

	@Override public void onAttachedToRecyclerView(RecyclerView recyclerView)
	{
		GridLayoutManager glm = new GridLayoutManager(recyclerView.getContext(), 3);
		glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return data.get(position).isGroup() ? 3 : 1;
			}
		});
		recyclerView.setLayoutManager(glm);
	}

	@Override public GroupedImageSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		int layoutId = viewType == GROUP ?
				R.layout.panorama_select_cell : R.layout.labeled_image_select_cell;
		View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(final GroupedImageSelectAdapter.ViewHolder viewHolder, final int position)
	{
		Item item = data.get(position);
		final boolean isSelected = selectedItem != null && data.indexOf(selectedItem) == position;

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
		if(selectedItem == null || prevSelectedItem != data.get(index))
		{
			selectedItem = data.get(index);
		} else {
			selectedItem = null;
		}

		if(selectedItem != null)
		{
			int selectedIndex = data.indexOf(selectedItem);
			notifyItemChanged(selectedIndex);

			if(selectedItem.isGroup())
			{
				if(prevSelectedItem == null || getGroup(data.indexOf(prevSelectedItem)) != selectedIndex )
				{
					expandGroup(selectedIndex);
				}
			}
		}
		if(prevSelectedItem != null)
		{
			int prevSelectedIndex = data.indexOf(prevSelectedItem);
			notifyItemChanged(prevSelectedIndex);

			int previousGroupIndex = getGroup(prevSelectedIndex);
			if(selectedItem == null || previousGroupIndex != getGroup(data.indexOf(selectedItem)))
			{
				retractGroup(previousGroupIndex);
			}
		}
	}

	private int getGroup(int index)
	{
		for (int i = index; i >= 0; i--)
		{
			if (data.get(i).isGroup()) return i;
		}
		return -1;
	}

	private void expandGroup(int index)
	{
		Item item = data.get(index);
		for (int i = 0; i < item.items.length; i++) {
			data.add(index + i + 1, item.items[i]);
		}
		notifyItemRangeInserted(index + 1, item.items.length);
	}

	private void retractGroup(int index)
	{
		Item item = data.get(index);
		for (int i = 0; i < item.items.length; i++) {
			data.remove(index + 1);
		}
		notifyItemRangeRemoved(index + 1, item.items.length);
	}

	@Override public int getItemCount()
	{
		return data.size();
	}

	@Override public int getItemViewType(int position)
	{
		return data.get(position).isGroup() ? GROUP : CELL;
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
			itemView.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					int index = getAdapterPosition();
					if(index != RecyclerView.NO_POSITION) onSelect(getAdapterPosition());
				}
			});
		}
	}

	public static class Item
	{
		public final int titleId;
		public final int drawableId;
		private final Item[] items;

		public Item(int drawableId, int titleId)
		{
			this(drawableId, titleId, null);
		}

		public Item(int drawableId, int titleId, Item[] items)
		{
			this.items = items;
			this.titleId = titleId;
			this.drawableId = drawableId;
		}

		private boolean isGroup() { return items != null; }
	}
}
