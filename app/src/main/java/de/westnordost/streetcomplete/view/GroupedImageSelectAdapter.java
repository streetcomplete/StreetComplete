package de.westnordost.streetcomplete.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.R;

/** Select one items from a groupable list of items */
public class GroupedImageSelectAdapter extends RecyclerView.Adapter<ItemViewHolder>
{
	private final static int GROUP = 0;
	private final static int CELL = 1;

	private int cellLayoutId = R.layout.cell_labeled_image_select;
	private int groupCellLayoutId = R.layout.cell_panorama_select;

	private ArrayList<Item> items = new ArrayList<>();
	private Item selectedItem;

	public interface OnItemSelectionListener
	{
		void onItemSelected(Item item);
	}
	private final List<OnItemSelectionListener> listeners = new ArrayList<>();

	public GroupedImageSelectAdapter(GridLayoutManager gridLayoutManager)
	{
		gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
		{
			@Override public int getSpanSize(int position)
			{
				return items.get(position).isGroup() ? gridLayoutManager.getSpanCount() : 1;
			}
		});
	}

	public void addOnItemSelectionListener(OnItemSelectionListener listener)
	{
		listeners.add(listener);
	}

	public void setCellLayout(int cellLayoutId)
	{
		this.cellLayoutId = cellLayoutId;
	}

	public void setGroupCellLayout(int groupCellLayoutId)
	{
		this.groupCellLayoutId = groupCellLayoutId;
	}

	public void setItems(List<Item> items)
	{
		this.items = new ArrayList<>(items);
		selectedItem = null;
		for (OnItemSelectionListener listener : listeners)
		{
			listener.onItemSelected(selectedItem);
		}
		notifyDataSetChanged();
	}

	@NonNull @Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		int layoutId = viewType == GROUP ? groupCellLayoutId : cellLayoutId;
		View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
		ItemViewHolder holder = new ItemViewHolder(view);
		holder.setOnClickListener(this::toggle);
		return holder;
	}

	@Override public void onBindViewHolder(@NonNull ItemViewHolder holder, int position)
	{
		holder.bind(items.get(position));
		holder.setSelected(selectedItem != null && items.indexOf(selectedItem) == position);
	}

	public Item getSelectedItem() { return selectedItem; }

	private void toggle(int index)
	{
		Item prevSelectedItem = selectedItem;
		if(selectedItem == null || prevSelectedItem != items.get(index))
		{
			selectedItem = items.get(index);
		} else {
			selectedItem = null;
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
		for (OnItemSelectionListener listener : listeners)
		{
			listener.onItemSelected(selectedItem);
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

	@Override public int getItemCount() { return items.size(); }

	@Override public int getItemViewType(int position)
	{
		return items.get(position).isGroup() ? GROUP : CELL;
	}
}
